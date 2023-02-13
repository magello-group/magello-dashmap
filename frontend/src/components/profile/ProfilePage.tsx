import React, {useEffect, useState} from 'react';
import {AuthenticatedTemplate, UnauthenticatedTemplate, useMsal} from "@azure/msal-react";
import {loginRequest} from "../../authConfig";
import {AuthenticationResult} from "@azure/msal-browser";
import {MagelloUser} from "../dataTypes/dataTypes";
import styled from "styled-components";
import {ProfileForm} from "./ProfileForm";

export const ProfilePage = (props: any) => {
    let {instance, accounts} = useMsal();
    const [isLoading, setIsLoading] = useState<boolean>(true)
    const [data, setData] = useState<MagelloUser | null>(null)
    const [token, setToken] = useState<string | null>(null)

    useEffect(() => {
        const request = {
            ...loginRequest,
            account: accounts[0]
        };

        // Silently acquires an access token which is then attached to a request for Microsoft Graph data
        instance.acquireTokenSilent(request).then((response: AuthenticationResult) => {
            setToken(response.accessToken);
        }).catch((e) => {
            return instance.acquireTokenPopup(request).then((response: AuthenticationResult) => {
                setToken(response.accessToken);
            });
        });
    }, [setToken, accounts, instance])

    useEffect(() => {
        if (!token) {
            return
        }

        fetch("http://localhost:8080/users/self", {
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
    }, [token])

    return (
        <ProfileHolder>
            <ProfileContents>
                <AuthenticatedTemplate>
                    {
                        data && <>
                            <ImageContainer>
                                <Image src={data.imageUrl}/>
                            </ImageContainer>
                            <ProfileForm userData={data} token={token}/>
                        </>
                    }
                    {
                        // TODO: write this
                    }
                </AuthenticatedTemplate>
                <UnauthenticatedTemplate>
                    {
                        // TODO: write this
                    }
                </UnauthenticatedTemplate>
            </ProfileContents>
        </ProfileHolder>
    );
}

const ProfileHolder = styled.div`
  display: flex;
  align-items: center;
  background-color: #f7f3f3;
  font-weight: 700;
  height: calc(100vh - 80px);
`

const ProfileContents = styled.div`
  padding: 2% 6%;
  height: 100%;
  box-sizing: border-box;
  transition: background .3s, border .3s, border-radius .3s, box-shadow .3s;
  display: flex;
  flex-direction: row;
  @media (max-width: 768px) {
    flex-direction: column;
  }
  margin-right: auto;
  margin-left: auto;
  align-items: flex-start;
`

const ImageContainer = styled.div`
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 25px;
  height: 200px;
  width: 200px;
  border-radius: 50%;
  box-shadow: 0px 0px 10px 0px rgba(0, 0, 0, 0.2);
`

const Image = styled.img`
  overflow: hidden;
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 100%;
  margin-bottom: 5px;
`
