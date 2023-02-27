import React, {useCallback} from "react";
import styled from "styled-components";
import {ProfileAreaStyle} from "./ProfileForm";
import {DefaultFormButton, FormButton, FormLabel} from "./components/FormComponents";
import {useLocation} from "react-router-dom";

export const AdminArea = ({token}: {token: string | null}) => {
    const location = useLocation();
    const onClick = useCallback(() => {
        const requestOptions = {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`
            },
        }

        fetch("http://localhost:8080/users/foodpreferences/export", requestOptions)
            .then((request) => {
                if (request.status >= 400) {
                    // TODO: Show a popup somehow
                    console.log("failed to send data!")
                } else {
                    request.blob().then((blob) => {
                        let file = window.URL.createObjectURL(blob)
                        let a = document.createElement('a')
                        a.href = file;
                        a.download = "export.csv"
                        document.body.appendChild(a)
                        a.click()
                        a.remove()
                    })
                }
            })
    }, [token]);

    return (
        <>
            <h3>Admin Area</h3>
            <AdminContent>
                <FormLabel>
                    <AdminButton type="button" value="Exportera matpreferenser" onClick={onClick}/>
                </FormLabel>
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
