import React, {useCallback} from "react";
import styled from "styled-components";
import {ProfileAreaStyle} from "./ProfileForm";
import {DefaultFormButton, FormLabel} from "./components/FormComponents";
import {toast} from "react-toastify";

export const AdminArea = ({token}: {token: string | null}) => {
    const onClick = useCallback(() => {
        const requestOptions = {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`
            },
        }

        fetch(`${process.env.REACT_APP_BACKEND_HOST}/users/foodpreferences/export`, requestOptions)
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
                    toast.error(() => (<div>Exportering av matpreferenser misslyckades<p style={{fontSize: "14px", fontWeight: 400}}>Fick status {response.status}, prova att refresha sidan eller fråga Fabian!</p></div>), {
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
