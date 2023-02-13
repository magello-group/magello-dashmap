import React from "react";
import { useMsal } from "@azure/msal-react";
import { loginRequest } from "../authConfig";
import {LoginLogoutButton} from "./SignOutButton";

export const SignInButton = () => {
    const { instance } = useMsal();

    const handleLogin = (loginType: any) => {
        if (loginType === "redirect") {
            instance.loginRedirect(loginRequest).catch(e => {
                console.log(e);
            });
        }
    }
    return (
        <LoginLogoutButton onClick={() => handleLogin("redirect")}>Logga in</LoginLogoutButton>
    );
}
