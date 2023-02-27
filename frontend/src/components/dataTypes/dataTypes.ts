export interface MagelloWorkAssignment {
    organisationId: string
    companyName: string
    longitude: number,
    latitude: number,
    users: StrippedMagelloUser[]

}

export interface PublicMagelloUser {
    id: number
    email: string
    firstName: string
    imageUrl?: string
    lastName: string
    title?: string
    skills: MagelloUserSkill[]
    assignment: MagelloWorkAssignment
    preferences?: PublicMagelloUserPreferences
}

export interface PublicMagelloUserPreferences {
    socials: SocialUrl[]
    quote?: string
}

export interface MagelloUser {
    id: number
    email: string
    firstName: string
    imageUrl?: string
    lastName: string
    isAdmin: boolean
    title?: string
    skills: MagelloUserSkill[]
    assignment: MagelloWorkAssignment
    preferences?: MagelloUserPreferences
}

export interface MagelloUserPreferences {
    dietPreferences: string[]
    extraDietPreferences?: string
    socials: SocialUrl[]
    quote?: string
}

export interface SocialUrl {
    url: string
}

export interface StrippedMagelloUser {
    id: number
    email: string
    firstName: string
    imageUrl?: string
    lastName: string
    title: string
    quote?: string
    userSkills: MagelloUserSkill[]
}

export interface MagelloUserSkill {
    id: number
    favourite?: boolean
    masterSynonym: string
    synonyms: string[]
    level?: number
    levelGoal?: number
    levelGoalDeadline?: string
    numberOfDaysWorkExperience?: number
}

export interface MagelloUserSkillWithUserInfo {
    id: number
    userId: number
    favourite?: boolean
    masterSynonym: string
    synonyms: string[]
    level?: number
    levelGoal?: number
    levelGoalDeadline?: string
    numberOfDaysWorkExperience?: number
    firstName: string
    lastName: string
}

export interface MagelloSkill {
    id: number
    masterSynonym: string
    synonyms: string[]
}
