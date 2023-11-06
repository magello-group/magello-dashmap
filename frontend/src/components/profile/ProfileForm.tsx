import React, {useCallback} from "react";
import {FoodPreferences} from "./FoodPreferences";
import styled, {css} from "styled-components";
import {MagelloUser} from "../dataTypes/dataTypes";
import {DividerSolid, FormButton, FormInput, FormLabel} from "./components/FormComponents";
import {Social} from "./Social";
import {SubmitHandler, useForm} from "react-hook-form";
import {MagelloFormPreferences, options} from "./formTypes";
import {toast, ToastContainer} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';


export const ProfileForm = ({userData, token}: { userData: MagelloUser, token: string | null }) => {
    const {
        register,
        handleSubmit,
        formState: {errors},
        watch,
        control
    } = useForm<MagelloFormPreferences>({
        defaultValues: {
            dietPreferences: userData.preferences ? userData.preferences.dietPreferences.map((val) => options.find(v => v.label === val)) : [],
            quote: userData.preferences?.quote,
            socials: userData.preferences?.socials,
            extraDietPreferences: userData.preferences?.extraDietPreferences
        }
    })

    const onSubmit: SubmitHandler<MagelloFormPreferences> = useCallback((data) => {
        const dataToSend = {
            dietPreferences: data.dietPreferences.filter(v => v !== undefined).map(v => v.label),
            extraDietPreferences: data.extraDietPreferences,
            socials: data.socials,
            quote: data.quote
        }
        const requestOptions = {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(dataToSend)
        }

        fetch(`${process.env.REACT_APP_BACKEND_HOST}/users/self/preferences`, requestOptions)
            .then((request) => {
                if (request.status === 204) {
                    toast.success("Ändringarna har sparats!", {
                        position: "bottom-center",
                        autoClose: 5000,
                        hideProgressBar: false,
                        closeOnClick: true,
                        pauseOnHover: true,
                        draggable: true,
                        progress: undefined,
                        theme: "light",
                    });
                } else {
                    toast.error(() => (<div>Fel när ändringarna sparades<p style={{fontSize: "14px", fontWeight: 400}}>Fråga Fabian eller prova igen senare</p></div>), {
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
    }, [])

    return (
        <Form onSubmit={handleSubmit(onSubmit)}>
            <FormLabel>Namn
                <FormInput type="text" value={userData.firstName + " " + userData.lastName} disabled={true}/>
            </FormLabel>
            <DividerSolid/>
            <FoodPreferences register={register} control={control}/>
            <DividerSolid/>
            <Social control={control} register={register} watch={watch}/>
            <DividerSolid/>
            <FormLabel>
                <FormButton type="submit" value="Spara"/>
            </FormLabel>
        </Form>
    )
}

export const ProfileAreaStyle = css`
  width: 30vw;
  border: 2px solid #ccc;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-content: center;

  @media (max-width: 768px) {
    max-width: 100%;
  }
`

const Form = styled.form`
  ${ProfileAreaStyle}
`

