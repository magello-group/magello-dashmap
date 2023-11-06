import React, {useCallback, useEffect, useState} from "react";
import styled from "styled-components";
import {ProfileAreaStyle} from "./ProfileForm";
import {DefaultFormButton, DividerSolid, FormLabel} from "./components/FormComponents";
import {toast} from "react-toastify";
import {MagelloUnmappedWorkplace} from "../dataTypes/dataTypes";
import {useNavigate} from "react-router-dom";

export const AdminArea = ({token}: { token: string | null }) => {
    const [unmappedWorkplaces, setUnmappedWorkplaces] = useState<MagelloUnmappedWorkplace[]>([])
    const navigate = useNavigate();
    useEffect(() => {
        const requestOptions = {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`
            },
        }

        fetch(`${process.env.REACT_APP_BACKEND_HOST}/admin/coordinates/unmapped`, requestOptions)
            .then((response) => {
                if (response.status === 200) {
                    response.json().then((data) => setUnmappedWorkplaces(data))
                } else {
                    toast.error(() => (<div>Kunde inte ladda in omappade arbetsplatser<p
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
    }, [token])

    const onClick = useCallback(() => {
        const requestOptions = {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`
            },
        }

        fetch(`${process.env.REACT_APP_BACKEND_HOST}/admin/foodpreferences/export`, requestOptions)
            .then((response) => {
                if (response.status === 200) {
                    response.blob().then((blob) => {
                        let file = window.URL.createObjectURL(blob);
                        let a = document.createElement('a');
                        a.href = file;
                        a.download = "food-preferences-export.csv";
                        document.body.appendChild(a);
                        a.click();
                        a.remove();
                    });
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
    }, [token]);

    const navigateToUpdateCoordinatesPage = useCallback(() => {
        navigate("/map-workplaces", {state: {unmapped: unmappedWorkplaces, token: token}})
    }, [unmappedWorkplaces, token])

    return (
        <>
            <h3>Admin Area</h3>
            <AdminContent>
                <FormLabel>
                    <AdminButton type="button" value="Exportera matpreferenser" onClick={onClick}/>
                </FormLabel>
                {
                    (unmappedWorkplaces && unmappedWorkplaces.length > 0) && <>
                        <DividerSolid/>
                        <FormLabel>Vi har hittat {unmappedWorkplaces.length} omappade arbetsplatser
                            <AdminButton type="button" value="Fixa mappningar" onClick={navigateToUpdateCoordinatesPage}/>
                        </FormLabel>
                    </>
                }
            </AdminContent>
        </>
    )
}

const AdminContent = styled.div`
  ${ProfileAreaStyle};
  border: 2px solid #fd3a3a;
`

const AdminButton = styled.input`
  ${DefaultFormButton};

  color: #fd3a3a;

  :hover {
    border: 1px #fd3a3a solid;

    :active {
      background-color: #fd3a3a;
      color: #fff;
    }
  }
`
