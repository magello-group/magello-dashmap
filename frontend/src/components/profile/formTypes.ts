import {SocialUrl} from "../dataTypes/dataTypes";

export interface MagelloFormPreferences {
    dietPreferences: Diet[]
    extraDietPreferences?: string
    socials: SocialUrl[]
    quote?: string
}

export interface Diet {
    value: string,
    label: string,
}

export const options: Diet[] = [
    {value: 'nuts', label: 'Nötallergi'},
    {value: 'vegan', label: 'Vegansk diet'},
    {value: 'vegetarian', label: 'Vegetarisk diet'},
    {value: 'eggs', label: 'Äggfri'},
    {value: 'lactose', label: 'Laktosfri'},
    {value: 'gluten', label: 'Glutenfri'},
    {value: 'pork', label: 'Fläskfri'},
    {value: 'fish', label: 'Fiskfri'},
];

