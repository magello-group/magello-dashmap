import React, {ChangeEvent, useCallback, useState} from "react";
import {FormInput, FormLabel} from "./components/FormComponents";
import {SocialIcon} from "react-social-icons";
import styled from "styled-components";

export interface SocialProps {
    // For now socials is only one
    socials?: string
    quote?: string
    setSocials: (event: ChangeEvent<any>) => void
    setQuote: (event: ChangeEvent<any>) => void
}

export const Social = ({socials, quote, setSocials, setQuote}: SocialProps) => {
    const [focused, setFocused] = useState<boolean>(false)

    const onFocus = useCallback(() => {
        setFocused(true)
    }, [setFocused])
    const onBlur = useCallback(() => {
        setFocused(false)
    }, [setFocused])

    return (
        <>
            <FormLabel>Social sida
                { /* TODO: Make this to its own component so that it can be reused. */ }
                <FormGroup>
                    <SuperSpan theme={{focus: focused}}>
                        <SocialIcon style={{maxHeight: "30px", maxWidth: "30px", pointerEvents: "none"}} url={socials}/>
                    </SuperSpan>
                    <SuperInput type="text" value={socials} onChange={setSocials} onFocus={onFocus} onBlur={onBlur}
                               placeholder={"https://github.com/"}/>
                </FormGroup>
            </FormLabel>
            <FormLabel>Schysst citat som visas under ditt namn
                <FormInput type="text" value={quote} onChange={setQuote} placeholder={"May the force be with you..."}/>
            </FormLabel>
        </>
    )
}

const SuperInput = styled.input`
  width: 100%;
  padding: 12px 20px;
  margin: 8px auto;
  display: inline-block;
  border: 1px solid #ccc;
  border-left: none;
  border-radius: 0 4px 4px 0;
  box-sizing: border-box;
  font-family: inherit;
  font-size: 1rem;

  :focus-visible {
    border: 1px #00aeef solid;
    border-left: none;
    outline: none;
  }
`

const SuperSpan = styled.span`
  background-color: #fff;
  border: 1px solid #ccc;
  border-radius: 4px 0 0 4px;
  padding: 0 8px;
  margin: 8px auto;
  text-align: center;
  display: flex;
  justify-items: center;
  align-items: center;

  ${props => props.theme.focus && 'border: 1px #00aeef solid'}
`

const FormGroup = styled.div`
  display: flex;
  
  :focus span {
    border: 1px #00aeef solid;
    border-left: none;
    outline: none;
  }
`
