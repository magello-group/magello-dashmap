import React, {useEffect, useState} from 'react';
import {AuthenticatedTemplate, UnauthenticatedTemplate, useMsal} from "@azure/msal-react";
import {loginRequest} from "../../authConfig";
import {AuthenticationResult} from "@azure/msal-browser";
import {MagelloUser, PublicMagelloUser} from "../dataTypes/dataTypes";
import styled from "styled-components";
import {ProfileForm} from "./ProfileForm";
import {WorkerImageCard} from "../map/WorkerImageCard";
import {AdminArea} from "./AdminArea";
import {useParams} from "react-router-dom";
import {ProfileContent} from "./ProfileContent";
import {toast} from "react-toastify";

export const ProfilePage = (props: any) => {
    const {instance, accounts} = useMsal();
    const {profileId} = useParams();
    const [isLoading, setIsLoading] = useState<boolean>(true)
    const [magelloUser, setMagelloUser] = useState<MagelloUser | null>(null)
    const [publicMagelloUser, setPublicMagelloUser] = useState<PublicMagelloUser | null>(null)
    const [token, setToken] = useState<string | null>(null)

    useEffect(() => {
        const request = {
            ...loginRequest,
            account: accounts[0]
        };

        // Silently acquires an access token which is then attached to a request for Microsoft Graph data
        instance.acquireTokenSilent(request).then((response: AuthenticationResult) => {
            setToken(response.accessToken);
        }).catch(async (e) => {
            const response = await instance.acquireTokenPopup(request);
            setToken(response.accessToken);
        });
    }, [setToken, accounts, instance])

    useEffect(() => {
        if (!token) {
            return
        }

        if (profileId) {
            fetch(`${process.env.REACT_APP_BACKEND_HOST}/api/users/${profileId}`, {
                method: 'GET',
                headers: {
                    "Accept": "application/json",
                    "Authorization": `Bearer ${token}`
                }
            }).then((response) => {
                if (response.status === 200) {
                    response.json().then((body) => {
                        setPublicMagelloUser(body);
                        setIsLoading(false);
                    })
                } else {
                    toast.error(() => (<div>Hämtning av profil misslyckades<p style={{fontSize: "14px", fontWeight: 400}}>Fick status {response.status}, prova att refresha sidan!</p></div>), {
                        position: "bottom-center",
                        autoClose: 5000,
                        hideProgressBar: false,
                        closeOnClick: true,
                        pauseOnHover: true,
                        draggable: true,
                        progress: undefined,
                        theme: "light",
                    });
                }
            });
        } else {
            fetch(`${process.env.REACT_APP_BACKEND_HOST}/api/users/self`, {
                method: 'GET',
                headers: {
                    "Accept": "application/json",
                    "Authorization": `Bearer ${token}`
                }
            }).then((response) => {
                if (response.status === 200) {
                    response.json().then((body) => {
                        setMagelloUser(body);
                        setIsLoading(false);
                    });
                } else {
                    toast.error(() => (<div>Hämtning av profil misslyckades<p style={{fontSize: "14px", fontWeight: 400}}>Fick status {response.status}, prova att refresha sidan!</p></div>), {
                        position: "bottom-center",
                        autoClose: 5000,
                        hideProgressBar: false,
                        closeOnClick: true,
                        pauseOnHover: true,
                        draggable: true,
                        progress: undefined,
                        theme: "light",
                    });
                }
            });
        }
    }, [token, profileId])

    return (
        <ProfileHolder>
            <ProfileContents>
                <AuthenticatedTemplate>
                    {
                        isLoading ? <div>Loading</div> : profileId
                            ? (
                                publicMagelloUser && <>
                                    <ImageContainer>
                                        <WorkerImageCard imageUrl={publicMagelloUser.imageUrl}/>
                                    </ImageContainer>
                                    <AreaContent>
                                        <ProfileContent userData={publicMagelloUser}/>
                                    </AreaContent>
                                </>
                            ) : (
                                magelloUser && <>
                                    <ImageContainer>
                                        <WorkerImageCard imageUrl={magelloUser.imageUrl}/>
                                    </ImageContainer>
                                    <AreaContent>
                                        <ProfileForm userData={magelloUser} token={token}/>
                                        {magelloUser.isAdmin && <AdminArea token={token}/>}
                                    </AreaContent>
                                </>
                            )
                    }
                </AuthenticatedTemplate>
                <UnauthenticatedTemplate>
                    {
                        // TODO: write this
                    }
                </UnauthenticatedTemplate>
            </ProfileContents>
        </ProfileHolder>
    );
}

const AreaContent = styled.div`
  display: flex;
  flex-direction: column;
`

const ProfileHolder = styled.div`
  display: flex;
  background-color: #f7f3f3;
  font-weight: 700;
  min-height: calc(100vh - 80px);
`

const ProfileContents = styled.div`
  padding: 2% 6%;
  height: 100%;
  box-sizing: border-box;
  transition: background .3s, border .3s, border-radius .3s, box-shadow .3s;
  display: flex;
  flex-direction: row;
  @media (max-width: 768px) {
    flex-direction: column;
  }
  margin-right: auto;
  margin-left: auto;
  align-items: flex-start;
`

const ImageContainer = styled.div`
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 25px;
  height: 200px;
  width: 200px;
  border-radius: 50%;
  box-shadow: 0px 0px 10px 0px rgba(0, 0, 0, 0.2);
`
