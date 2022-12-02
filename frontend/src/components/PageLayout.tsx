import React from "react";
import Navbar from "react-bootstrap/Navbar";
import {useIsAuthenticated} from "@azure/msal-react";
import {SignInButton} from "./SignInButton";
import {SignOutButton} from "./SignOutButton";

/**
 * Renders the navbar component with a sign-in button if a user is not authenticated
 */
export const PageLayout = (props: any) => {
    const isAuthenticated = useIsAuthenticated();

    return (
        <>
            <Navbar bg="primary" variant="dark">
                <a className="navbar-brand" href="/">Magello Dashmap</a>
                {isAuthenticated ? <span>Signed In <SignOutButton/></span> : <SignInButton/>}
            </Navbar>
            <h5>
                <div style={{textAlign: "center"}}>
                    Welcome to the Microsoft Authentication Library For React Tutorial
                </div>
            </h5>
            <br/>
            <br/>
            {props.children}
        </>
    );
};
