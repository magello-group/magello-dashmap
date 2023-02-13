import React from 'react';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import {PublicClientApplication} from "@azure/msal-browser";
import {MsalProvider} from "@azure/msal-react";
import {msalConfig} from "./authConfig";
import ReactDOM from "react-dom/client";
import {createGlobalStyle} from "styled-components";

const AppCss = createGlobalStyle`
  body, html {
    font-family: "Roboto", "sans-serif";
  }
`

const msalInstance = new PublicClientApplication(msalConfig);

const root = ReactDOM.createRoot(document.getElementById("root") as HTMLElement);

// StrictMode apparently make some useEffects being called twice :O
// Read more here: https://stackoverflow.com/questions/61254372/my-react-component-is-rendering-twice-because-of-strict-mode
root.render(
    <React.StrictMode>
        <MsalProvider instance={msalInstance}>
            <AppCss/>
            <App/>
        </MsalProvider>
    </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
