import React, {useEffect, useMemo, useState} from "react";
import {loginRequest} from "../../authConfig";
import {AuthenticationResult} from "@azure/msal-browser";
import {useMsal} from "@azure/msal-react";
import {useParams} from "react-router-dom";
import styled from "styled-components";
import {MagelloUserSkillWithUserInfo} from "../dataTypes/dataTypes";
import {useFlexLayout, useSortBy, useTable} from "react-table"
import {IoArrowDown, IoArrowUp} from "react-icons/io5";
import {GoDash} from "react-icons/go";

export const SkillPage = () => {
    const {instance, accounts} = useMsal();
    const {skillId} = useParams();
    const [userSkills, setUserSkills] = useState<MagelloUserSkillWithUserInfo[] | undefined>(undefined);
    const [skillName, setSkillName] = useState<string>("");
    const [token, setToken] = useState<string | null>(null)

    const columns = useMemo(() => {
        return [
            {
                Header: "Förnamn",
                accessor: "firstName"
            },
            {
                Header: "Efternamn",
                accessor: "lastName"
            },
            {
                Header: "Nivå",
                accessor: "level"
            }
        ]
    }, [])

    useEffect(() => {
        const request = {
            ...loginRequest,
            account: accounts[0]
        };

        // Silently acquires an access token which is then attached to a request for Microsoft Graph data
        instance.acquireTokenSilent(request).then((response: AuthenticationResult) => {
            setToken(response.accessToken);
        }).catch(() => {
            return instance.acquireTokenPopup(request).then((response: AuthenticationResult) => {
                setToken(response.accessToken);
            });
        });
    }, [setToken, accounts, instance])

    useEffect(() => {
        if (!token) {
            return;
        }

        fetch(`http://localhost:8080/skill/${skillId}`, {
            method: 'GET',
            headers: {
                "Accept": "application/json",
                "Authorization": `Bearer ${token}`
            }
        }).then((response) => response.json().then((result: MagelloUserSkillWithUserInfo[]) => {
            setSkillName(result[0].masterSynonym);
            setUserSkills(result);
        }));
    }, [token, skillId, setUserSkills, setSkillName])

    const data = useMemo(() => userSkills ? userSkills : [], [userSkills])

    return (
        <SkillDisplayArea>
            <SkillArea>
                <SkillName>{skillName}</SkillName>
                {userSkills && <SkillTable skills={data} columns={columns}/>}
            </SkillArea>
        </SkillDisplayArea>
    )
}

const SkillTable = ({skills, columns}: { skills: MagelloUserSkillWithUserInfo[], columns: any }) => {
    const {
        getTableProps,
        getTableBodyProps,
        headerGroups,
        rows,
        prepareRow,
    } = useTable({
            columns: columns,
            data: skills,
        },
        useSortBy
    );

    // TODO: style
    return (
        <TableStyles>
            <table {...getTableProps()}>
                <thead>
                {headerGroups.map((headerGroup) => (
                    <tr {...headerGroup.getHeaderGroupProps()}>
                        {headerGroup.headers.map((column: any) => (
                            <th {...column.getHeaderProps(column.getSortByToggleProps())}>
                                <div>
                                    {column.render('Header')}
                                    <span style={{width: "20px"}}>
                                    {
                                        column.isSorted
                                            ? column.isSortedDesc
                                                ? <IoArrowDown/>
                                                : <IoArrowUp/>
                                            : <GoDash/>
                                    }
                                    </span>
                                </div>
                            </th>
                        ))}
                    </tr>
                ))}
                </thead>
                <tbody {...getTableBodyProps()}>
                {rows.map((row, i) => {
                    prepareRow(row)
                    return (
                        <tr {...row.getRowProps()}>
                            {row.cells.map(cell => {
                                return <td {...cell.getCellProps()}>{cell.render('Cell')}</td>
                            })}
                        </tr>
                    )
                })}
                </tbody>
            </table>
        </TableStyles>
    )
}

const SkillDisplayArea = styled.div`
  padding: 2% 6%;
  box-sizing: border-box;
  transition: background .3s, border .3s, border-radius .3s, box-shadow .3s;
  display: block;
  background-color: #fff;
`

const SkillArea = styled.div`
  min-height: 50%;
  max-width: 1300px;
  align-items: center;
  display: flex;
  margin-right: auto;
  margin-left: auto;
  flex-wrap: wrap;
  flex-direction: column;
  justify-content: space-evenly;
`

const SkillName = styled.div`
  width: 1300px;
  margin-right: auto;
  margin-left: auto;
  margin-bottom: 20px;
  font-size: 28px;
  font-weight: 700;
`

const TableStyles = styled.div`
  max-width: 100%;
  display: block;
  padding: 1rem;

  table {
    border-spacing: 0;
    border: 1px solid #ccc;
    border-radius: 4px;
    font-family: "Roboto", "sans-serif";

    tr {
      :last-child {
        td {
          border-bottom: 0;
        }
      }
    }

    th,
    td {
      margin: 0;
      padding: 0.5rem;
      border-bottom: 1px solid #ccc;
      border-right: 1px solid #ccc;

      :last-child {
        border-right: 0;
      }
    }

    th {
      div {
        display: flex;
        flex-direction: row;
        justify-content: space-between;
      }
    }
  }
`
