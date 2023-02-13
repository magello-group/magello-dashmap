import React from "react";
import {StrippedMagelloUser} from "../dataTypes/dataTypes";
import styled from "styled-components";

const quotes = [
    "Jag gillar att inte behöva jobba!",
    "Jag är 25 år gammal",
    "Hej! Jag kan flyga!",
    "Visste du att valar är det största däggdjuret som någonsin funnits på vår jord!",
    "Jag har ätit pannkaka!",
    "Man kan ju göra så... Men det är ju inte rätt!",
    "Blanda inte in mig i vårt förhållande!",
    "T-röd, för dig som tänkt klart"
]

export const WorkerCard = ({worker}: { worker: StrippedMagelloUser }) => {
    return (
        <Content>
            {worker.imageUrl ? <Image src={worker.imageUrl}/> :
                <StockImage src="https://magello.se/wp-content/uploads/2019/05/Kattux-start-head.svg"/>
            }
            <Name>{worker.firstName} {worker.lastName}</Name>
            {worker.quote && (<Quote>{worker.quote ? worker.quote : ""}</Quote>)}
        </Content>
    )
}

const Content = styled.div`
  padding: 1% 1%;
  width: 240px;
  min-height: 320px;
  display: inline-block;
  text-align: center;
  border-radius: 10px 10px;
  margin-bottom: 10px;
`

const Image = styled.img`
  border-radius: 50%;
  overflow: hidden;
  display: inline-block;
  height: 200px;
  width: 200px;
  margin-bottom: 5px;
  box-shadow: 0px 0px 10px 0px rgba(0, 0, 0, 0.2);
`

const StockImage = styled.img`
  border-radius: 50%;
  overflow: hidden;
  display: inline-block;
  height: 200px;
  width: 200px;
  transform: rotate(0.5turn);
  margin-bottom: 5px;
  box-shadow: 0px 0px 10px 0px rgba(0, 0, 0, 0.2);
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
