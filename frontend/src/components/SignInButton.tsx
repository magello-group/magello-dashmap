import React from "react";
import { useMsal } from "@azure/msal-react";
import { loginRequest } from "../authConfig";
import styled from "styled-components";

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
        <LoginButton onClick={() => handleLogin("redirect")}>Logga in</LoginButton>
    );
}

const LoginButton = styled.button`
  margin-top: 4px;
  padding-left: 20px;
  padding-right: 20px;
  width: 160px;
  height: 30px;
  background-image: url("/button.svg");
  border: none;
  font-weight: 300;
  background-color: transparent;
  :hover{
    background-color: #00aeef;
  }
`
