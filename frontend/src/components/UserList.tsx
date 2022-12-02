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
            <table className="table table-striped table-dark">
                <thead>
                <tr>
                    <th scope="col">Förnamn</th>
                    <th scope="col">Efternamn</th>
                    <th scope="col">Titel</th>
                </tr>
                </thead>
                <tbody>
                {
                    userData.map((user) => {
                        return (<UserRow user={user}/>)
                    })
                }
                </tbody>
            </table>
        </>
    )
}


const UserRow = ({user}: {user: User}) => {
    return (
        <tr>
            <td>{user.firstName}</td>
            <td>{user.lastName}</td>
            <td>{user.title ? user.title : "-"}</td>
        </tr>
    )
}
