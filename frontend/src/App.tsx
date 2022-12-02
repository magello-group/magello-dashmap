import React from 'react';
import './App.css';
import {PageLayout} from "./components/PageLayout";
import {AuthenticatedTemplate, UnauthenticatedTemplate} from "@azure/msal-react";
import {ProfilePage} from "./components/ProfilePage";

function App() {
    return (
        <PageLayout>
            <AuthenticatedTemplate>
                <ProfilePage/>
            </AuthenticatedTemplate>
            <UnauthenticatedTemplate>
                <p>You are not signed in! Please sign in.</p>
            </UnauthenticatedTemplate>
        </PageLayout>
    );
}

export default App;
