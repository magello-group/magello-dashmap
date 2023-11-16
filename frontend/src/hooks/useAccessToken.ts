import React, {useEffect, useState} from "react";
import {useMsal} from "@azure/msal-react";
import {loginRequest} from "../authConfig";
import {AccountInfo, AuthenticationResult} from "@azure/msal-browser";

export const useAccessToken = (): [string | null, AccountInfo[], boolean] => {
    const {instance, accounts, inProgress} = useMsal();
    const [accessToken, setAccessToken] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(true)

    useEffect(() => {
        const request = {
            ...loginRequest,
            account: accounts[0]
        };

        // Silently acquires an access token which is then attached to a request for Microsoft Graph data
        instance.acquireTokenSilent(request).then((response: AuthenticationResult) => {
            setAccessToken(response.accessToken);
            setIsLoading(false);
            return response.accessToken;
        }).catch(async (e) => {
            try {
                let response = await instance.acquireTokenPopup(request);
                setAccessToken(response.accessToken);
                setIsLoading(false);

                return response.accessToken;
            } catch (error) {
                return console.log(error);
            }
        });
    }, [instance, accounts])

    return [accessToken, accounts, isLoading]
}
