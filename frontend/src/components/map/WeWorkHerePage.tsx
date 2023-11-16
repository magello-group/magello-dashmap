import React, {useEffect, useState} from "react";
import {MagelloWorkAssignment, MagelloWorkPlace} from "../dataTypes/dataTypes";
import styled from "styled-components";
import {WorkerCard} from "./WorkerCard";
import {useAccessToken} from "../../hooks/useAccessToken";
import {toast} from "react-toastify";

export const WeWorkHerePage = ({workplace}: { workplace: MagelloWorkPlace | null }) => {
    const [accessToken, _, tokenIsLoading] = useAccessToken();
    const [currentWorkplace, setCurrentWorkplace] = useState<MagelloWorkAssignment>()
    const [isLoading, setIsLoading] = useState(tokenIsLoading);

    useEffect(() => {
        if (!accessToken) {
            return;
        }

        fetch(`${process.env.REACT_APP_BACKEND_HOST}/api/workplaces/${workplace?.organisationId}`, {
            method: 'GET',
            headers: {
                "Accept": "application/json",
                "Authorization": `Bearer ${accessToken}`
            }
        }).then((response) => {
            if (response.status === 200) {
                response.json().then((body) => {
                    setCurrentWorkplace(body);
                    setIsLoading(false);
                })
            } else {
                toast.error(() => (
                    <div>Hämtning av profil misslyckades<p style={{fontSize: "14px", fontWeight: 400}}>Fick
                        status {response.status}, prova att refresha sidan!</p></div>), {
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
    }, [accessToken, workplace]);

    return (
        isLoading
            ? <div>Loading...</div>
            : currentWorkplace
                ? (<WorkDisplayArea>
                    <WorkplaceName>
                        <b>{currentWorkplace.companyName}</b> -
                        {currentWorkplace.companyName === 'Magello'
                            ? " Vi är mellan uppdrag"
                            : " Det är vi som jobbar här!"
                        }
                    </WorkplaceName>
                    <WorkerArea>
                        {
                            currentWorkplace.users.map((worker) => {
                                return (<WorkerCard key={worker.id} worker={worker}/>)
                            })
                        }
                    </WorkerArea>
                </WorkDisplayArea>)
                : (<div/>)
    )
}

const WorkDisplayArea = styled.div`
  position: absolute;
  // To hide the map attribution when clicked
  z-index: 1000;
  border-top-left-radius: 20px;
  border-top-right-radius: 20px;
  top: calc(50vh);
  margin-left: auto;
  margin-right: auto;
  left: 0;
  right: 0;
  padding: 2% 6%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  transition: background .3s, border .3s, border-radius .3s, box-shadow .3s;
  background-color: #fff;
`

const WorkerArea = styled.div`
  align-items: center;
  display: flex;
  flex-direction: row;
  margin-right: 15vw;
  margin-left: 15vw;
  flex-wrap: wrap;
  justify-content: space-evenly;
`

const WorkplaceName = styled.div`
  max-width: 1300px;
  margin-right: auto;
  margin-left: auto;
  margin-bottom: 20px;
  font-size: 28px;
  font-weight: 400;
`
