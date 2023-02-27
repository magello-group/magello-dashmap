import {SocialUrl} from "../dataTypes/dataTypes";

export interface MagelloFormPreferences {
    dietPreferences: Diet[]
    extraDietPreferences?: string
    socials: SocialUrl[]
    quote?: string
}

export interface Diet {
    value: number,
    label: string,
}

export const options: Diet[] = [
    {value: 0, label: 'Nötallergi'},
    {value: 1, label: 'Veganskt'},
    {value: 2, label: 'Vegetariskt'},
    {value: 3, label: 'Äggfri'},
    {value: 4, label: 'Laktosfri'},
    {value: 5, label: 'Glutenfri'},
    {value: 6, label: 'Fläskfri'},
    {value: 7, label: 'Fiskfri'},
];

