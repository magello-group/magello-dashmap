import React, {useEffect, useState} from "react";
import {useMsal} from "@azure/msal-react";
import {loginRequest} from "../authConfig";
import {AccountInfo, AuthenticationResult} from "@azure/msal-browser";

export const useAccessToken = (): [string | null, AccountInfo[], boolean] => {
    const {instance, accounts, inProgress} = useMsal();
    const [accessToken, setAccessToken] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(true)

    const requestAccessToken = () => {
        const request = {
            ...loginRequest,
            account: accounts[0]
        };

        // Silently acquires an access token which is then attached to a request for Microsoft Graph data
        return instance.acquireTokenSilent(request).then((response: AuthenticationResult) => {
            setAccessToken(response.accessToken);
            setIsLoading(false);
            return response.accessToken;
        }).catch((e) => {
            return instance.acquireTokenPopup(request).then((response: AuthenticationResult) => {
                setAccessToken(response.accessToken);
                setIsLoading(false);

                return response.accessToken;
            });
        });
    }

    // TODO: make this usable
    useEffect(() => {
        requestAccessToken().then()
    }, [instance, accounts])

    return [accessToken, accounts, isLoading]
}
