export const msalConfig = {
    auth: {
        clientId: "f0f1dafe-726f-425f-9345-8807aec46532",
        authority: "https://login.microsoftonline.com/0ef3ca2b-0b6f-4543-b1f3-eb1e7ec2069f",
    },
    cache: {
        cacheLocation: "sessionStorage", // This configures where your cache will be stored
        storeAuthStateInCookie: false, // Set this to "true" if you are having issues on IE11 or Edge
    }
};

// Add scopes here for ID token to be used at Microsoft identity platform endpoints.
export const loginRequest = {
    scopes: ["api://f0f1dafe-726f-425f-9345-8807aec46532/Users.Read"]
};

// Add the endpoints here for Microsoft Graph API services you'd like to use.
export const graphConfig = {
    graphMeEndpoint: "https://graph.microsoft.com/v2.0/me"
};

