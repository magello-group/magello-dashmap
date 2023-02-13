import React from "react";
import Select from "react-select";
import makeAnimated from 'react-select/animated';
import {FormInput, FormLabel} from "./components/FormComponents";
import styled from "styled-components";
import {Control, Controller, UseFormRegister} from "react-hook-form";
import {Diet, MagelloFormPreferences, options} from "./formTypes";

export interface FoodPreferencesProps {
    register: UseFormRegister<MagelloFormPreferences>
    control: Control<MagelloFormPreferences, any>
}

export const FoodPreferences = ({register, control}: FoodPreferencesProps) => {
    return (
        <>
            <FormLabel>Dietära preferenser
                <DietaryMultiSelect control={control}/>
            </FormLabel>
            <FormLabel>Övriga dietära preferenser
                <FormInput type="text" {...register("extraDietPreferences")} placeholder={"Jag äter inte sjögräs..."}/>
            </FormLabel>
        </>
    );
}

interface DietaryMultiSelectProps {
    control: Control<MagelloFormPreferences, any>
}

const DietaryMultiSelect = ({control}: DietaryMultiSelectProps) => {
    const animatedComponents = makeAnimated()

    return (
        <Controller name={"dietPreferences"}
                    control={control}
                    render={({field: {value, ref, onChange, onBlur}}) => (
                        <MultiSelect
                            inputRef={ref}
                            placeholder={"Välj en eller flera..."}
                            value={value}
                            options={options}
                            onChange={onChange}
                            onBlur={onBlur}
                            getOptionLabel={(diet: Diet) => diet.label}
                            getOptionValue={(diet: Diet) => diet.value}
                            components={animatedComponents}
                            isMulti={true}
                            backspaceRemovesValue={true}
                            isClearable={true}
                        />
                    )}/>
    )
}

const StyledSelect = styled(Select)`
  font-weight: 400;
`

const MultiSelect = (props: any) => <StyledSelect multi {...props} />
