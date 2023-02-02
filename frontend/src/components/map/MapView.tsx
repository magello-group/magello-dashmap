import React, {useEffect, useState} from "react";
import {MapContainer, Marker, Popup, TileLayer, useMap} from "react-leaflet";
import styled from "styled-components";
import {WeWorkHerePage} from "./WeWorkHerePage";
import {MagelloWorkAssignment} from "../dataTypes/dataTypes";
import {PopupEvent} from "leaflet";
import {UnauthenticatedTemplate} from "@azure/msal-react";

export const MapView = (props: {path: string, isLoading: boolean, data: MagelloWorkAssignment[] | null}) => {
    const [currentWorkplace, setCurrentWorkplace] = useState<MagelloWorkAssignment | null>(null)

    return (
        <>
            <UnauthenticatedTemplate>
                <MapHolder>
                    {props.isLoading ? <div/> :
                        <StyledMapContainer center={[59.325, 18.07]}
                                      zoom={10} scrollWheelZoom={false}>
                            <TileLayer
                                attribution='&copy; <a href="https://stadiamaps.com/">Stadia Maps</a>, &copy; <a href="https://openmaptiles.org/">OpenMapTiles</a> &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors'
                                url="https://tiles.stadiamaps.com/tiles/alidade_smooth_dark/{z}/{x}/{y}{r}.png"
                                maxZoom={20}
                            />
                            <Workplaces data={props.data ? props.data : []} currentWorkplace={currentWorkplace}
                                        setCurrentWorkplace={setCurrentWorkplace}/>
                        </StyledMapContainer>
                    }
                </MapHolder>
                <WeWorkHerePage currentWorkplace={currentWorkplace}/>
            </UnauthenticatedTemplate>
        </>
    )
}

const Workplaces = ({
                        data,
                        currentWorkplace,
                        setCurrentWorkplace
                    }: { data: MagelloWorkAssignment[], currentWorkplace: MagelloWorkAssignment | null, setCurrentWorkplace: React.Dispatch<React.SetStateAction<MagelloWorkAssignment | null>> }) => {
    const map = useMap();

    useEffect(() => {
        map.on({
            popupopen: (event: PopupEvent) => {
                const latLng = event.popup.getLatLng();
                if (latLng) {
                    // Should we zoom in closer?
                    map.flyTo(latLng, map.getZoom(), {
                        duration: 1.0,
                        animate: true,
                        easeLinearity: 0.25
                    });
                }
                const workplace = data.find((workplace) => {
                    return event.popup.getContent() === workplace.companyName
                });
                if (workplace) {
                    setCurrentWorkplace(workplace);
                }
            }
        })
    }, [data, map, setCurrentWorkplace])

    /*
    // Fly around functionality. TODO: Should be paused if the user is moving around the map
    // Maybe use this as a 'play' functionality which can be triggered when displayed on a conference screen.
    useEffect(() => {
        const t = setInterval(() => {
            if (data) {
                const workplace = data[Math.floor(Math.random() * data.length)]
                if (workplace.organisationId !== currentWorkplace?.organisationId) {
                    map.flyTo([workplace.latitude, workplace.longitude], 16, {
                        duration: 2.5,
                        animate: true,
                        easeLinearity: 0.25
                    });

                    setCurrentWorkplace(workplace);
                }
            }
        }, 10000);

        return () => {
            clearTimeout(t);
        }
        // setTimer(timer);
    }, [currentWorkplace])
    */

    return (
        <>
            {
                data.map((workplace) => {
                    return (
                        <Marker key={workplace.organisationId} position={[workplace.latitude, workplace.longitude]}>
                            <Popup content={workplace.companyName}/>
                        </Marker>
                    )
                })
            }
        </>
    )
}

const MapHolder = styled.div`
  width: 100%;
  min-height: 50%;
  // TODO: How can we render map without setting height to a fixed size?
  height: 800px;
  max-height: 50%;
  margin-right: auto;
  margin-left: auto;
  align-items: center;
  display: flex;
  overflow: hidden;
`

const StyledMapContainer = styled(MapContainer)`
  height: 100%;
  minHeight: 100%;
  width: 100%;
`
