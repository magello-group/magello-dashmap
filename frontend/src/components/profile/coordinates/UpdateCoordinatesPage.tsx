import React, {useCallback, useEffect, useState} from "react";
import styled from "styled-components";
import {redirect, useLocation, useNavigate} from "react-router-dom";
import {toast} from "react-toastify";
import {MapWorkplace} from "./components/MapWorkplace";
import {LocationMarkerUpdate} from "./dataTypes";

export const UpdateCoordinatesPage = () => {
    const {state} = useLocation();
    const navigate = useNavigate();
    const [currentLocation, setCurrentLocation] = useState<number>(0);

    const onPageChange = useCallback((update: LocationMarkerUpdate) => {
        const requestOptions = {
            method: "PUT",
            headers: {
                "Authorization": `Bearer ${state.token}`
            },
        }

        fetch(`${process.env.REACT_APP_BACKEND_HOST}/admin/coordinates/${update.organisationId}/edit?longitude=${update.latLng.lng}&latitude=${update.latLng.lat}`, requestOptions)
            .then((response) => {
                if (response.status === 204) {
                    toast.success(() => (
                        <div>{state.unmapped[currentLocation].companyName} uppdaterad!</div>
                    ));
                    setCurrentLocation(prevState => prevState + 1);
                } else {
                    toast.error(() => (<div>Exportering av matpreferenser misslyckades<p
                        style={{fontSize: "14px", fontWeight: 400}}>Fick status {response.status}, prova att refresha
                        sidan eller fråga Fabian!</p></div>), {
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
            })
    }, [state, currentLocation])

    useEffect(() => {
        if (state) {
            if (currentLocation >= state.unmapped.length) {
                navigate('/')
            }
        } else {
            navigate('/')
        }
    }, [state, currentLocation]);

    return (
        <>
            <UnmappedArea>
                <UnmappedContent>
                    {state && (currentLocation < state.unmapped.length)
                        ? <MapWorkplace selectedWorkplace={state.unmapped[currentLocation]}
                                        onNext={onPageChange}
                        />
                        : null
                    }
                </UnmappedContent>
            </UnmappedArea>
        </>
    )
}

const UnmappedArea = styled.div`
  display: flex;

  justify-content: center;
  align-content: center;
  width: 100%;
`

const UnmappedContent = styled.div`
  margin-top: 20px;
  width: 50vw;
`
