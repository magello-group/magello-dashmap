import React from "react";
import { useMsal } from "@azure/msal-react";
import Button from "react-bootstrap/Button";
import styled from "styled-components";

/**
 * Renders a button which, when selected, will redirect the page to the logout prompt
 */
export const SignOutButton = () => {
    const { instance } = useMsal();

    const handleLogout = (logoutType: any) => {
        if (logoutType === "redirect") {
            instance.logoutRedirect({
                postLogoutRedirectUri: "/",
            });
        }
    }

    return (
        <LoginLogoutButton onClick={() => handleLogout("redirect")}>Logga ut</LoginLogoutButton>
    );
}

export const LoginLogoutButton = styled.button`
  margin-top: 4px;
  padding-left: 20px;
  padding-right: 20px;
  width: 160px;
  height: 30px;
  background-image: url("/button.svg");
  border: none;
  font-weight: 300;
  font-family: inherit;
  font-size: 1rem;
  background-color: transparent;
  :hover{
    background-color: #00aeef;
  }
`
