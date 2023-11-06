import React, {ReactNode, useCallback, useContext, useEffect, useState} from "react";
import {MapContainer, Marker, Popup, TileLayer, useMap} from "react-leaflet";
import styled, {css} from "styled-components";
import {WeWorkHerePage} from "./WeWorkHerePage";
import {MagelloWorkAssignment, Mapped} from "../dataTypes/dataTypes";
import * as L from "leaflet";
import {MarkerClusterGroup, MarkerClusterGroupOptions, PopupEvent} from "leaflet";
import 'leaflet.markercluster';
import {WorkplaceContext} from "../../App";
import {createElementObject, createLayerComponent, EventedProps, extendContext} from "@react-leaflet/core";


const latLng1 = L.latLng(55, 11);
const latLng2 = L.latLng(63, 20);

export const maxBounds = L.latLngBounds(latLng2, latLng1);

export const MapView = () => {
    const {workplaces: data, isLoading, reload} = useContext(WorkplaceContext);
    const [currentWorkplace, setCurrentWorkplace] = useState<MagelloWorkAssignment | null>(null)

    useEffect(() => {
        reload();
    }, []);

    const mapContent = useCallback(() => {
        return (isLoading ? <div/> : <StyledMapContainer center={[59.325, 18.07]} maxBounds={maxBounds} minZoom={8}
                                                         zoom={12} scrollWheelZoom={false}>
            <TileLayer
                attribution='&copy; <a href="https://stadiamaps.com/">Stadia Maps</a>, &copy; <a href="https://openmaptiles.org/">OpenMapTiles</a> &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors'
                url="https://tiles-eu.stadiamaps.com/tiles/alidade_smooth_dark/{z}/{x}/{y}{r}.png"
                maxZoom={20}
            />
            <Workplaces data={data ? data : []}
                        setCurrentWorkplace={setCurrentWorkplace}/>
        </StyledMapContainer>)
    }, [currentWorkplace, data, isLoading]);

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
                        setCurrentWorkplace
                    }: {
    data: MagelloWorkAssignment[],
    setCurrentWorkplace: React.Dispatch<React.SetStateAction<MagelloWorkAssignment | null>>
}) => {
    const map = useMap();

    useEffect(() => {
        const clusterGroup = L.markerClusterGroup();
        const filtered = data.filter((workplace) => {
            return workplace.coordinates.type === "mapped";
        });
        filtered.forEach(mapped => {
            const mappedCoordinates = mapped.coordinates as Mapped;
            const marker = L.marker([mappedCoordinates.lat, mappedCoordinates.lon]);
            marker.bindPopup(`${mapped.companyName}`);
            clusterGroup.addLayer(marker);
        })

        map.addLayer(clusterGroup);

        map.on({
            popupopen: (event: PopupEvent) => {
                const latLng = event.popup.getLatLng();
                if (latLng) {
                    // Call `invalidateSize` when we know we have set the size of the workplace area and then use flyTo.
                    setTimeout(() => {
                        map.invalidateSize(true);
                        // Should we zoom in closer?
                        map.flyTo(latLng, map.getZoom(), {
                            duration: 1.0,
                            animate: true,
                            easeLinearity: 0.25,
                        });
                    }, 100)
                }
                // @ts-ignore
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
                    try {
                        map.invalidateSize(true);
                    } catch (e) {
                        console.log("Failed to invalidate size since we have lost its reference");
                    }
                }, 100)
            }
        })

        return () => {
            map.removeLayer(clusterGroup);
        }
    }, [map, data, setCurrentWorkplace]);

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

    return null;
}

const baseMapHolder = css`
  width: 100%;
  margin-right: auto;
  margin-left: auto;
  align-items: center;
  display: flex;
  overflow: hidden;
`

const MapHolderFullscreen = styled.div`
  height: calc(100vh - 80px);
  ${baseMapHolder}
`

const StyledMapContainer = styled(MapContainer)`
  height: 100%;
  min-height: 100%;
  width: 100%;
`
