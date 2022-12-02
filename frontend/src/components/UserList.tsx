import React from "react";

export interface User {
    companyUserEmail?: string
    companyUserId: number
    companyUserType?: number
    createdDateTime: string
    firstName: string
    lastName: string
    title?: string
    updatedDateTime?: string
}

export const UserList = ({userData}: {userData: User[]}) => {
    return (
        <>
            {
                userData.map((user) => {
                    return (<p>{user.firstName} {user.lastName}{user.title ? ` - Titel: ${user.title}` : ""} </p>)
                })
            }
        </>
    )
}
