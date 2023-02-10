import React, {ChangeEvent, useCallback, useEffect, useState} from "react";
import Select, {ActionMeta, OnChangeValue} from "react-select";
import makeAnimated from 'react-select/animated';
import {FormLabel, FormTextArea} from "./components/FormComponents";
import styled from "styled-components";

export interface FoodPreferencesProps {
    dietPreferences?: string
    extraDietPreferences?: string
    setDiet: (diet: string) => void
    setDietExtras: (event: ChangeEvent<any>) => void
}

export const FoodPreferences = ({
                                    dietPreferences,
                                    extraDietPreferences,
                                    setDiet,
                                    setDietExtras
                                }: FoodPreferencesProps) => {
    return (
        <>
            <FormLabel>Dietära preferenser
                <DietaryMultiSelect dietPreferences={dietPreferences} setDiet={setDiet}/>
            </FormLabel>
            <FormLabel>Övriga dietära preferenser
                <FormTextArea value={extraDietPreferences} onChange={setDietExtras}
                              placeholder={"Jag äter inte sjögräs..."}/>
            </FormLabel>
        </>
    );
}

const options: Diet[] = [
    {value: 'nuts', label: 'Nötallergi'},
    {value: 'vegan', label: 'Vegansk diet'},
    {value: 'vegetarian', label: 'Vegetarisk diet'},
    {value: 'eggs', label: 'Äggfri'},
    {value: 'lactose', label: 'Laktosfri'},
    {value: 'gluten', label: 'Glutenfri'},
    {value: 'pork', label: 'Fläskfri'},
    {value: 'fish', label: 'Fiskfri'},
];

interface Diet {
    value: string,
    label: string,
}

interface DietaryMultiSelectProps {
    dietPreferences?: string
    setDiet: (diet: string) => void
}

const DietaryMultiSelect = ({dietPreferences, setDiet}: DietaryMultiSelectProps) => {
    const [selectedOptions, setSelectedOptions] = useState<Diet[]>(
        dietPreferences
            ? dietPreferences.split(";")
                .map((diet) => options.find(value => diet === value.value))
                .filter(diet => diet) as Diet[]
            : []
    );
    const animatedComponents = makeAnimated()

    const onChange = useCallback((value: OnChangeValue<Diet, true>, actionMeta: ActionMeta<Diet>) => {
        switch (actionMeta.action) {
            case 'remove-value':
            case 'pop-value':
                setSelectedOptions((prevState) => prevState.filter((v) => v.value != actionMeta.removedValue.value));
                break;
            case 'clear':
                setSelectedOptions([]);
                break;
            case "create-option":
            case "select-option":
                setSelectedOptions((prevState) => {
                    if (actionMeta.option) {
                        const newState = [
                            actionMeta.option,
                            ...prevState
                        ];
                        return newState;
                    }

                    return prevState;
                })
        }
    }, [])

    useEffect(() => {
        setDiet(selectedOptions.map((d) => d.value).join(";"))
    }, [selectedOptions])

    return (
        <MultiSelect
            placeholder={"Välj en eller flera..."}
            value={selectedOptions}
            options={options}
            onChange={onChange}
            components={animatedComponents}
            getOptionLabel={(diet: Diet) => diet.label}
            getOptionValue={(diet: Diet) => diet.value}
            isMulti={true}
            backspaceRemovesValue={true}
            isClearable={true}
        />
    )
}

const StyledSelect = styled(Select)`
  font-weight: 400;
`

const MultiSelect = (props: any) => <StyledSelect multi {...props} />
