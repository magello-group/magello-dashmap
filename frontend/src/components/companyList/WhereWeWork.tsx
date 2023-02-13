import React, {useCallback, useContext, useState} from "react";
import {MagelloWorkAssignment} from "../dataTypes/dataTypes";
import styled, {ThemeProvider} from "styled-components";
import {WorkplaceContext} from "../../App";

export const WhereWeWork = () => {
    const {workplaces} = useContext(WorkplaceContext);
    return (
        <WorkDisplayArea>
            <Title>Vi Magelliter jobbar här!</Title>
            {
                workplaces?.map((item) => (
                    <WorkListItem key={item.organisationId} item={item}/>
                ))
            }
        </WorkDisplayArea>
    )
}

const WorkListItem = ({item}: { item: MagelloWorkAssignment }) => {
    const [active, setActive] = useState<boolean>(false)

    const open = useCallback(() => {
        setActive((prev) => !prev)
    }, [setActive])

    return (
        <WorkSection>
            <ThemeProvider theme={{opened: active}}>
                <Button type="button" onClick={open}>{item.companyName}</Button>
                {active && <WorkContent>
                    <>
                        {
                            item.users.map((user) => {
                                return (
                                    // Link to the user page
                                    <p>{user.firstName} {user.lastName}</p>
                                )
                            })
                        }
                    </>
                </WorkContent>
                }
            </ThemeProvider>
        </WorkSection>
    )
}

const WorkSection = styled.div`
  width: 80%;
  margin-right: auto;
  margin-left: auto;
`

const Button = styled.button`
  margin-top: 5px;
  border-radius: 10px 10px ${props => props.theme.opened ? 0 : "10px"} ${props => props.theme.opened ? 0 : "10px"};
  background-color: ${props => props.theme.opened ? "#eaeaea" : "#fff"};
  color: black;
  padding: 18px;
  width: 100%;
  border: none;
  text-align: left;
  outline: none;
  font-size: 18px;

  :hover {
    background-color: #eaeaea;
  }
`

const Title = styled.h3`
  margin-right: auto;
  margin-left: auto;
  margin-bottom: 20px;
  font-size: 28px;
  font-weight: 700;
`

const WorkContent = styled.div`
  padding: 18px 18px;
  overflow: hidden;
  margin-bottom: 5px;
  background-color: #fff;
  border-bottom-left-radius: 10px;
  border-bottom-right-radius: 10px;
`

const WorkDisplayArea = styled.div`
  background-color: #f7f3f3;
  padding: 2% 6% 6% 6%;
  box-sizing: border-box;
  transition: background .3s, border .3s, border-radius .3s, box-shadow .3s;
  height: 100%;
  display: block;
`
