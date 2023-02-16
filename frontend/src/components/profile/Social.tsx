import React, {useCallback, useState} from "react";
import {FormButton, FormInput, FormLabel} from "./components/FormComponents";
import {SocialIcon} from "react-social-icons";
import styled from "styled-components";
import {Control, useFieldArray, UseFieldArrayRemove, UseFormRegister, UseFormWatch} from "react-hook-form";
import {MagelloFormPreferences} from "./formTypes";
import {IoClose} from "react-icons/io5";

export interface SocialProps {
    control: Control<MagelloFormPreferences>
    register: UseFormRegister<MagelloFormPreferences>
    watch: UseFormWatch<MagelloFormPreferences>
}

export const Social = ({register, watch, control}: SocialProps) => {
    const {fields, append, remove} = useFieldArray({
        name: "socials",
        control: control
    });

    return (
        <>
            <FormLabel>Schysst citat som visas under ditt namn
                <FormInput type="text" {...register("quote")} placeholder={"May the force be with you..."}/>
            </FormLabel>
            <FormLabel>Social sida
                {fields.map((field, index) => (
                    <SocialPageInput key={field.id} register={register} index={index} watch={watch} remove={remove}/>
                ))}
                <FormButton type="button" onClick={() => append({url: ""})} value="Lägg till ett till länk fält"/>
            </FormLabel>
        </>
    )
}

interface SocialPageInputProps {
    register: UseFormRegister<MagelloFormPreferences>
    index: number
    watch: UseFormWatch<MagelloFormPreferences>
    remove: UseFieldArrayRemove
}

const SocialPageInput = (props: SocialPageInputProps) => {
    const [focused, setFocused] = useState<boolean>(false)

    const onFocus = useCallback(() => {
        setFocused(true)
    }, [setFocused])
    const onBlur = useCallback(() => {
        setFocused(false)
    }, [setFocused])

    const watch = props.watch(`socials.${props.index}.url`);

    return (
        <FormGroup>
            <SocialIconSpan theme={{focus: focused}}>
                <SocialIcon style={{maxHeight: "30px", maxWidth: "30px", pointerEvents: "none"}} url={watch}/>
            </SocialIconSpan>
            <SocialInput type="text" {...props.register(`socials.${props.index}.url` as const)} onFocus={onFocus}
                        onBlur={onBlur}
                        placeholder={"https://github.com/..."}/>
            <RemoveSpan theme={{focus: focused}} onClick={() => props.remove(props.index)}><IoClose/></RemoveSpan>
        </FormGroup>
    )
}

const SocialInput = styled.input`
  width: 100%;
  padding: 12px 20px;
  margin: 8px auto;
  display: inline-block;
  border: 1px solid #ccc;
  border-left: none;
  border-right: none;
  box-sizing: border-box;
  font-family: inherit;
  font-size: 1rem;

  :focus-visible {
    border: 1px #00aeef solid;
    border-left: none;
    border-right: none;
    outline: none;
  }
`

const SocialIconSpan = styled.span`
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

const RemoveSpan = styled.span`
  background-color: #fff;
  border: 1px solid #ccc;
  border-radius: 0 4px 4px 0;
  padding: 0 8px;
  margin: 8px auto;
  text-align: center;
  display: flex;
  align-items: center;
  pointer-events: all;
  cursor: pointer;

  :hover {
    background-color: #f6f6f6;
    border: 1px #00aeef solid;
    :active {
      background-color: #00aeef;
    }
  }

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
