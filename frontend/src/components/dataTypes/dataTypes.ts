export interface MagelloWorkAssignment {
    organisationId: string
    companyName: string
    longitude: number,
    latitude: number,
    users: StrippedMagelloUser[]

}

export interface MagelloUser {
    id: number
    email: string
    firstName: string
    imageUrl?: string
    lastName: string
    title?: string
    skills: MagelloSkill[]
    assignment: MagelloWorkAssignment
    preferences?: MagelloUserPreferences
}

export interface MagelloUserPreferences {
    dietPreferences?: string
    extraDietPreferences?: string
    socials?: string
    quote?: string
}

export interface StrippedMagelloUser {
    id: number
    email: string
    firstName: string
    imageUrl?: string
    lastName: string
    title: string
}

export interface MagelloSkill {
    id: number
    favourite?: boolean
    masterSynonym: string
    synonyms: string[]
    level?: number
    levelGoal?: number
    levelGoalDeadline?: string
    numberOfDaysWorkExperience?: number
}
