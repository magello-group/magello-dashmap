import React, {ChangeEvent, useCallback, useState} from "react";
import {FoodPreferences} from "./FoodPreferences";
import styled from "styled-components";
import {MagelloUser, MagelloUserPreferences} from "../dataTypes/dataTypes";
import {FormButton, FormInput, FormLabel} from "./components/FormComponents";
import {Social} from "./Social";

export const ProfileForm = ({userData, token}: { userData: MagelloUser, token: string | null }) => {
    const [userPreferences, setUserPreferences] = useState<MagelloUserPreferences>(userData.preferences ? userData.preferences : {})
    const submit = useCallback((event: any) => {
        event.preventDefault();

        const requestOptions = {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(userPreferences)
        }

        fetch("http://localhost:8080/users/self/preferences", requestOptions)
            .then((request) => {
                if (request.status >= 400) {
                    // TODO: Show a popup somehow
                    console.log("failed to send data!")
                }
            })
    }, [userPreferences, token])

    const changeDiet = useCallback((diet: string) => {
        setUserPreferences((prevState) => {
            return {
                ...prevState,
                dietPreferences: diet
            }
        })
    }, [setUserPreferences])
    const changeDietExtras = useCallback((dietExtras: ChangeEvent<any>) => {
        setUserPreferences((prevState) => {
            return {
                ...prevState,
                extraDietPreferences: dietExtras.target.value
            }
        })
    }, [setUserPreferences])
    const changeSocials = useCallback((socials: ChangeEvent<any>) => {
        setUserPreferences((prevState) => {
            return {
                ...prevState,
                socials: socials.target.value
            }
        })
    }, [setUserPreferences])
    const changeQuote = useCallback((quote: ChangeEvent<any>) => {
        setUserPreferences((prevState) => {
            return {
                ...prevState,
                quote: quote.target.value
            }
        })
    }, [setUserPreferences])

    return (
        <Form onSubmit={submit}>
            <FormLabel>Namn
                <FormInput type="text" value={userData.firstName + " " + userData.lastName} disabled={true}/>
            </FormLabel>
            <FoodPreferences dietPreferences={userPreferences.dietPreferences}
                             extraDietPreferences={userPreferences.extraDietPreferences}
                             setDiet={changeDiet}
                             setDietExtras={changeDietExtras}/>
            <Social quote={userPreferences.quote}
                    setQuote={changeQuote}
                    socials={userPreferences.socials}
                    setSocials={changeSocials}/>
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
