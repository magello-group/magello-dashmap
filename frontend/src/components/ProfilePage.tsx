import React, {useState} from 'react';
import {useMsal} from "@azure/msal-react";
import {loginRequest} from "../authConfig";
import {AuthenticationResult} from "@azure/msal-browser";
import {Button} from "react-bootstrap";
import {User, UserList} from "./UserList";

export const ProfilePage = () => {
    const { instance, accounts, inProgress } = useMsal();
    const [accessToken, setAccessToken] = useState<string | null>(null);
    const [data, setData] = useState<User[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    const name = accounts[0] && accounts[0].name;

    const RequestAccessToken = () => {
        const request = {
            ...loginRequest,
            account: accounts[0]
        };

        // Silently acquires an access token which is then attached to a request for Microsoft Graph data
        return instance.acquireTokenSilent(request).then((response: AuthenticationResult) => {
            setAccessToken(response.accessToken);
            return response.accessToken;
        }).catch((e) => {
            return instance.acquireTokenPopup(request).then((response: AuthenticationResult) => {
                setAccessToken(response.accessToken);

                return response.accessToken;
            });
        });
    }

    const getUserData = async () => {
        if (!accessToken) {
            const token = await RequestAccessToken();
            fetchUsers(token)
        } else {
            fetchUsers(accessToken)
        }
    }

    const fetchUsers = (token: string) => {
        fetch("http://localhost:8080/users?offset=2&limit=5", {
            method: 'GET',
            headers: {
                "Accept": "application/json",
                "Authorization": `Bearer ${token}`
            }
        }).then((response) => {
            response.json().then((body) => {
                setData(body);
                setIsLoading(false);
            })
        })
    }

    return (
        <>
            <h5 className="card-title">Welcome {name}</h5>
            {isLoading ?
                <Button variant="primary" onClick={getUserData}>Fetch user data</Button>
                :
                <UserList userData={data} />
            }
        </>
    );
}
