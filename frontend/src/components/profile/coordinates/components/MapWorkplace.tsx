import {MagelloUnmappedWorkplace} from "../../../dataTypes/dataTypes";
import React, {useCallback, useEffect, useRef, useState} from "react";
import {LatLng} from "leaflet";
import {maxBounds} from "../../../map/MapView";
import {MapContainer, TileLayer} from "react-leaflet";
import styled from "styled-components";
import {UnmappedLocationMarker} from "./UnmappedLocationMarker";
import {LocationMarkerUpdate} from "../dataTypes";
import {toast} from "react-toastify";

export const MapWorkplace = (props: {
    selectedWorkplace: MagelloUnmappedWorkplace,
    onNext: (update: LocationMarkerUpdate) => void
}) => {
    const [position, setPosition] = useState<LatLng | null>(null)
    const temp = useRef<HTMLInputElement>(null);

    const onSave = useCallback(() => {
        if (position) {
            props.onNext({
                organisationId: props.selectedWorkplace.organisationId,
                latLng: position
            });
        } else {
            toast.info(() => (<div>Men du!<p
                style={{fontSize: "14px", fontWeight: 400}}>Välj först en plats på kartan, tryck sedan på knappen</p></div>), {
                position: "bottom-center",
                autoClose: 5000,
                hideProgressBar: false,
                closeOnClick: true,
                pauseOnHover: true,
                draggable: true,
                progress: undefined,
                theme: "light",
            });
        }
    }, [position])

    useEffect(() => {
        setPosition(null);
        if (temp.current) {
            temp.current.style.animation = 'none'
            setTimeout(() => {
                if (temp.current) {
                    temp.current.style.animation = '';
                }
            }, 50)
        }
    }, [props.selectedWorkplace]);

    return (
        <>
            <div style={{position: 'relative'}}>
                <MapHolderFullscreen>
                    <StyledMapContainer center={[59.325, 18.07]}
                                        maxBounds={maxBounds}
                                        minZoom={8}
                                        zoom={12}
                                        scrollWheelZoom={false}
                    >
                        <UnmappedLocationMarker companyName={props.selectedWorkplace.companyName}
                                                position={position}
                                                setPosition={setPosition}/>
                        <TileLayer
                            attribution='&copy; <a href="https://stadiamaps.com/">Stadia Maps</a>, &copy; <a href="https://openmaptiles.org/">OpenMapTiles</a> &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors'
                            url="https://tiles.stadiamaps.com/tiles/alidade_smooth_dark/{z}/{x}/{y}{r}.png"
                            maxZoom={20}
                        />
                    </StyledMapContainer>
                    <MapTitle ref={temp}>
                        Ange koordinater för <b>{props.selectedWorkplace.companyName}</b>
                    </MapTitle>
                    <SaveButton type="button" onClick={onSave}>Spara</SaveButton>
                </MapHolderFullscreen>
            </div>
        </>
    )
}

const MapHolderFullscreen = styled.div`
  margin-top: 25px;
  height: 35vh;
  width: 100%;
  margin-right: auto;
  margin-left: auto;
  align-items: center;
  display: flex;
  overflow: hidden;
`

const StyledMapContainer = styled(MapContainer)`
  height: 100%;
  min-height: 100%;
  width: 100%;
`

export const SaveButton = styled.button`
  margin-top: 4px;
  padding-left: 20px;
  padding-right: 20px;
  width: 159px;
  height: 29px;
  background-image: url("/button-green.svg");
  border: none;
  font-weight: 300;
  font-family: inherit;
  font-size: 1rem;
  background-color: transparent;
  position: absolute;
  bottom: 20px;
  left: 45%;
  z-index: 999;

  :hover {
    background-color: #12a765;
  }
`

const MapTitle = styled.div`
  animation-name: blinker;
  animation-duration: 1s;
  animation-iteration-count: 3;
  animation-timing-function: linear;
  position: absolute;
  z-index: 999;
  color: white;
  top: 20px;
  left: 70px;

  @keyframes blinker {
    50% {
      opacity: 0;
    }
  }
`
