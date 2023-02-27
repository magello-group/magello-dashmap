import React from "react";
import styled from "styled-components";

export const WorkerImageCard = ({imageUrl}: { imageUrl?: string }) => {
    return (
        <>
            {imageUrl ? <Image src={imageUrl}/> :
                <StockImage src="https://magello.se/wp-content/uploads/2019/05/Kattux-start-head.svg"/>
            }
        </>
    );
}

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

