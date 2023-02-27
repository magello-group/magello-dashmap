import React, {useCallback, useContext, useEffect, useState} from "react";
import {MapContainer, Marker, Popup, TileLayer, useMap} from "react-leaflet";
import styled, {css} from "styled-components";
import {WeWorkHerePage} from "./WeWorkHerePage";
import {MagelloWorkAssignment} from "../dataTypes/dataTypes";
import {PopupEvent} from "leaflet";
import {WorkplaceContext} from "../../App";

export const MapView = () => {
    const {workplaces: data, isLoading} = useContext(WorkplaceContext);
    const [currentWorkplace, setCurrentWorkplace] = useState<MagelloWorkAssignment | null>(null)

    const mapContent = useCallback(() => {
        return (isLoading ? <div/> : <StyledMapContainer center={[59.325, 18.07]}
                                                         zoom={10} scrollWheelZoom={false}>
            <TileLayer
                attribution='&copy; <a href="https://stadiamaps.com/">Stadia Maps</a>, &copy; <a href="https://openmaptiles.org/">OpenMapTiles</a> &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors'
                url="https://tiles.stadiamaps.com/tiles/alidade_smooth_dark/{z}/{x}/{y}{r}.png"
                maxZoom={20}
            />
            <Workplaces data={data ? data : []} currentWorkplace={currentWorkplace}
                        setCurrentWorkplace={setCurrentWorkplace}/>
        </StyledMapContainer>)
    }, [currentWorkplace, data, isLoading]);

    useEffect(() => {
        console.log("currentWorkplace:", currentWorkplace)
    }, [currentWorkplace])

    return (
        <>
            <MapHolderFullscreen style={currentWorkplace ? {height: "calc(55vh - 80px)"} : undefined}>
                {mapContent()}
            </MapHolderFullscreen>
            {currentWorkplace && <WeWorkHerePage currentWorkplace={currentWorkplace}/>}
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
                    // Call `invalidateSize` when we know we have set the size of the workplace area and then use flyTo.
                    setTimeout(() => {
                        map.invalidateSize(true);
                        // Should we zoom in closer?
                        map.flyTo(latLng, 11.5, {
                            duration: 1.0,
                            animate: true,
                            easeLinearity: 0.25,
                        });
                    }, 100)
                }
                const workplace = data.find((workplace) => {
                    return event.popup.getContent() === workplace.companyName
                });
                if (workplace) {
                    setCurrentWorkplace(workplace);
                }
            },
            popupclose: () => {
                setCurrentWorkplace(null);

                setTimeout(() => {
                    map.invalidateSize(true);
                }, 100)
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

const baseMapHolder = css`
  width: 100%;
  margin-right: auto;
  margin-left: auto;
  align-items: center;
  display: flex;
  overflow: hidden;
`

const MapHolderExpanded = styled.div`
  height: 50vh;
  ${baseMapHolder}
`

const MapHolderFullscreen = styled.div`
  height: calc(100vh - 80px);
  ${baseMapHolder}
`

const StyledMapContainer = styled(MapContainer)`
  height: 100%;
  minHeight: 100%;
  width: 100%;
`
