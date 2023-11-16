import {useEffect, useState} from "react";
import {useMsal} from "@azure/msal-react";
import {loginRequest} from "../authConfig";
import {AccountInfo, AuthenticationResult} from "@azure/msal-browser";

export const useAccessToken = (): {accessToken: string | null, accounts: AccountInfo[], tokenLoading: boolean} => {
    const {instance, accounts} = useMsal();
    const [accessToken, setAccessToken] = useState<string | null>(null);
    const [tokenLoading, setTokenLoading] = useState<boolean>(true)

    useEffect(() => {
        const request = {
            ...loginRequest,
            account: accounts[0]
        };

        // Silently acquires an access token which is then attached to a request for Microsoft Graph data
        instance.acquireTokenSilent(request).then((response: AuthenticationResult) => {
            setAccessToken(response.accessToken);
            setTokenLoading(false);
            return response.accessToken;
        }).catch(async (e) => {
            try {
                let response = await instance.acquireTokenPopup(request);
                setAccessToken(response.accessToken);
                setTokenLoading(false);

                return response.accessToken;
            } catch (error) {
                return console.log(error);
            }
        });
    }, [instance, accounts])

    return {accessToken, accounts, tokenLoading}
}
