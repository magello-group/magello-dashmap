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
