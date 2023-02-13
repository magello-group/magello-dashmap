import React, {useCallback} from "react";
import {FoodPreferences} from "./FoodPreferences";
import styled from "styled-components";
import {MagelloUser} from "../dataTypes/dataTypes";
import {FormButton, FormInput, FormLabel} from "./components/FormComponents";
import {Social} from "./Social";
import {SubmitHandler, useForm} from "react-hook-form";
import {MagelloFormPreferences, options} from "./formTypes";


export const ProfileForm = ({userData, token}: { userData: MagelloUser, token: string | null }) => {
    const {
        register,
        handleSubmit,
        formState: {errors},
        watch,
        control
    } = useForm<MagelloFormPreferences>({
        defaultValues: {
            dietPreferences: userData.preferences?.dietPreferences.map((val) => options.find(v => v.value === val)),
            quote: userData.preferences?.quote,
            socials: userData.preferences?.socials,
            extraDietPreferences: userData.preferences?.extraDietPreferences
        }
    })

    const onSubmit: SubmitHandler<MagelloFormPreferences> = useCallback((data) => {
        const dataToSend = {
            dietPreferences: data.dietPreferences.map(v => v.value),
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

        fetch("http://localhost:8080/users/self/preferences", requestOptions)
            .then((request) => {
                if (request.status >= 400) {
                    // TODO: Show a popup somehow
                    console.log("failed to send data!")
                }
            })
    }, [])

    return (
        <Form onSubmit={handleSubmit(onSubmit)}>
            <FormLabel>Namn
                <FormInput type="text" value={userData.firstName + " " + userData.lastName} disabled={true}/>
            </FormLabel>
            <FoodPreferences register={register} control={control}/>
            <Social control={control} register={register} watch={watch}/>
            <FormButton type="submit" value="Spara"/>
        </Form>
    )
}

const Form = styled.form`
  max-width: 30vw;

  @media (max-width: 768px) {
    max-width: 100%;
  }
`
