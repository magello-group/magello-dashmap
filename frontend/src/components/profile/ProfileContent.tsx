import React, {useMemo} from "react";
import {MagelloUserSkill, PublicMagelloUser} from "../dataTypes/dataTypes";
import styled from "styled-components";
import {ProfileAreaStyle} from "./ProfileForm";
import {DividerSolid, FormLabel} from "./components/FormComponents";
import {SocialIcon} from "react-social-icons";
import {Column, Row, useSortBy, useTable} from "react-table";
import {IoArrowDown, IoArrowUp} from "react-icons/io5";
import {GoDash} from "react-icons/go";
import {useNavigate} from "react-router-dom";
import {AiFillStar} from "react-icons/ai";
import {BsDash} from "react-icons/bs";

interface ProfileContentProps {
    userData: PublicMagelloUser
}

export function ProfileContent({userData}: ProfileContentProps) {
    const skills = useMemo(() => userData.skills, [userData])
    const navigate = useNavigate();

    const sort = useMemo(() => (rowA: Row<MagelloUserSkill>, rowB: Row<MagelloUserSkill>, columnId: String, desc: boolean) => {
        if (rowA.original.favourite === undefined) return -1;
        if (rowB.original.favourite === undefined) return 1;
        if (rowA.original.favourite > rowB.original.favourite) return 1;
        if (rowA.original.favourite < rowB.original.favourite) return -1;
        return 0
    }, [])

    const cols = useMemo(() => {
        return [
            {
                Header: "Kompetens",
                accessor: "masterSynonym"
            },
            {
                Header: "Nivå",
                accessor: "level"
            },
            {
                Header: "Favorit",
                accessor: "favourite",
                Cell: ({value}) => (value ? <AiFillStar/> : <BsDash/>),
                sortType: sort
            }
        ] as Column[]
    }, []);

    const {
        getTableProps,
        getTableBodyProps,
        headerGroups,
        rows,
        prepareRow,
    } = useTable({
            columns: cols,
            data: skills,
        },
        useSortBy
    );

    return (
        <Content>
            <FormLabel>Namn
                <Text>{userData.firstName + " " + userData.lastName}</Text>
            </FormLabel>
            <DividerSolid/>
            <FormLabel>Citat
                <Text>{userData.preferences?.quote ? userData.preferences.quote : `${userData.firstName} har inte skrivit något citat än`}</Text>
            </FormLabel>
            <FormLabel>Sociala sidor
                {userData.preferences?.socials ? userData.preferences?.socials?.map((socialUrl) => (
                    <FormGroup key={socialUrl.url}>
                        <SocialLink href={socialUrl.url} target="_blank">
                            <SocialIconTextSpan>
                                <SocialIcon style={{maxHeight: "30px", maxWidth: "30px", pointerEvents: "none"}}
                                            url={socialUrl.url}
                                            as={"div"}
                                />
                            </SocialIconTextSpan>
                            <SocialText>{socialUrl.url}</SocialText>
                        </SocialLink>
                    </FormGroup>
                )) : <Text>{userData.firstName} har inte fyllt i några sociala sidor än</Text>}
            </FormLabel>
            <DividerSolid/>
            <FormLabel>Kompetenser
                <CompetenceTable>
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
                        {rows.map((row) => {
                            prepareRow(row);
                            return (
                                <tr onClick={() =>
                                    // Id does exist...
                                    // @ts-ignore
                                    navigate(`/skill/${row.original.id}`)
                                }
                                    {...row.getRowProps()}
                                >
                                    {row.cells.map(cell => {
                                        return <td {...cell.getCellProps()}>{cell.render('Cell')}</td>
                                    })}
                                </tr>
                            )
                        })}
                        </tbody>
                    </table>
                </CompetenceTable>
            </FormLabel>
        </Content>
    );
}

const Content = styled.div`
  ${ProfileAreaStyle}
`

export const Text = styled.p`
  width: 100%;
  padding: 12px 20px;
  margin: 8px auto;
  display: inline-block;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
  font-family: inherit;
  font-weight: 400;
  font-size: 1rem;
  background-color: #fff;

  :focus-visible {
    outline: 1px #00aeef solid;
  }
`

const SocialText = styled.p`
  width: 100%;
  padding: 12px 20px;
  display: inline-block;
  border: 1px solid #ccc;
  border-radius: 0 4px 4px 0;
  border-left: none;
  box-sizing: border-box;
  font-family: inherit;
  font-weight: 400;
  font-size: 1rem;
  margin: 0;
  background-color: #fff;
  cursor: pointer;
  pointer-events: inherit;

  :focus-visible {
    border: 1px #00aeef solid;
    border-left: none;
    border-right: none;
    outline: none;
  }
`

const SocialIconTextSpan = styled.span`
  background-color: #fff;
  border: 1px solid #ccc;
  border-radius: 4px 0 0 4px;
  padding: 0 8px;
  text-align: center;
  display: flex;
  justify-items: center;
  align-items: center;
`

const FormGroup = styled.div`
  margin: 8px 0;
`

const SocialLink = styled.a`
  display: flex;
`

const CompetenceTable = styled.div`
  max-width: 100%;
  display: block;
  padding: 1rem;

  table {
    width: 100%;
    border-spacing: 0;
    border: 1px solid #ccc;
    border-radius: 4px;
    font-family: "Roboto", "sans-serif";

    tbody {
      tr {
        cursor: pointer;
        pointer-events: all;

        :hover {
          background-color: #ccc;
        }
        :active {
          background-color: #9f9f9f;
        }
      }
    }

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
      font-weight: 400;

      :last-child {
        border-right: 0;
      }
    }

    th {
      div {
        font-weight: 700;
        display: flex;
        flex-direction: row;
        justify-content: space-between;
      }
    }
  }
`
