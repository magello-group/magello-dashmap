import React from "react";
import {MagelloWorkAssignment} from "../dataTypes/dataTypes";
import styled from "styled-components";
import {WorkerCard} from "./WorkerCard";

export const WeWorkHerePage = ({currentWorkplace}: { currentWorkplace: MagelloWorkAssignment | null }) => {
    return (
        currentWorkplace ?
            <WorkDisplayArea>
                <WorkplaceName>Det är vi som jobbar här!</WorkplaceName>
                <WorkerArea>
                    {
                        currentWorkplace.users.map((worker) => {
                            return (<WorkerCard key={worker.id} worker={worker}/>)
                        })
                    }
                </WorkerArea>
            </WorkDisplayArea> : <div/>
    )
}

const WorkDisplayArea = styled.div`
  position: absolute;
  // To hide the map attribution when clicked
  z-index: 1000;
  border-top-left-radius: 20px;
  border-top-right-radius: 20px;
  top: calc(50vh);
  margin-left: auto;
  margin-right: auto;
  left: 0;
  right: 0;
  padding: 2% 6%;
  box-sizing: border-box;
  transition: background .3s, border .3s, border-radius .3s, box-shadow .3s;
  display: block;
  background-color: #fff;
`

const WorkerArea = styled.div`
  min-height: 50%;
  max-width: 1300px;
  align-items: center;
  display: flex;
  margin-right: auto;
  margin-left: auto;
  flex-wrap: wrap;
  justify-content: space-evenly;
`

const WorkplaceName = styled.div`
  max-width: 1300px;
  margin-right: auto;
  margin-left: auto;
  margin-bottom: 20px;
  font-size: 28px;
  font-weight: 700;
`
