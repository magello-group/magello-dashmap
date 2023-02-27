import React, {useCallback} from "react";
import {StrippedMagelloUser} from "../dataTypes/dataTypes";
import styled from "styled-components";
import {WorkerImageCard} from "./WorkerImageCard";
import {useNavigate} from "react-router-dom";

export const WorkerCard = ({worker}: { worker: StrippedMagelloUser }) => {
    const navigate = useNavigate();
    const navigateToUser = useCallback(() => {
        navigate(`/profile/${worker.id}`)
    }, [])

    const favouriteUserSkills = worker.userSkills.filter((skill) => skill?.favourite === true);
    return (
        <Content onClick={navigateToUser}>
            <WorkerImageCard imageUrl={worker.imageUrl}/>
            <Name>{worker.firstName} {worker.lastName}</Name>
            {worker.quote && (<Quote>{worker.quote ? worker.quote : ""}</Quote>)}
            <SkillArea>
                {favouriteUserSkills.slice(0, Math.min(3, favouriteUserSkills.length)).map((skill) => (
                    <SkillBadge>{skill.masterSynonym} | {skill.level}</SkillBadge>
                ))}
            </SkillArea>
        </Content>
    )
}

const SkillArea = styled.div`
  display: flex;
  flex-wrap: wrap;
`

const SkillBadge = styled.div`
  background-color: var(--magello-color-blue);
  color: #fff;
  box-shadow: 0 2px 1px -1px #0003, 0 1px 1px #00000024, 0 1px 3px #0000001f;
  height: 23px;
  line-height: 23px;
  min-width: 23px;
  border-radius: 16px;
  display: flex;
  position: relative;
  font-weight: 600;
  margin: 2px 2px;
  padding: 0 6px;
  font-size: 11px;
`

const Content = styled.div`
  cursor: pointer;
  padding: 1% 1%;
  width: 240px;
  min-height: 320px;
  display: inline-block;
  text-align: center;
  border-radius: 10px 10px;
  margin-bottom: 10px;

  :hover {
    box-shadow: 0px 0px 10px 0px rgba(0, 0, 0, 0.2);
  }

  :active {
    background-color: rgba(0, 0, 0, 0.2);
  }
`

const Name = styled.div`
  line-height: 24px;
  margin-bottom: 5px;
  font-size: 18px;
  font-weight: 500;
  text-align: center;
`

const Quote = styled.q`
  inline-size: 150px;
  overflow-wrap: break-word;
  hyphens: manual;
  quotes: '"' '"';
  font-style: italic;

  ::before {
    content: open-quote;
  }

  ::after {
    content: close-quote;
  }
`
