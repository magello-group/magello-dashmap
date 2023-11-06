import {LatLng, LeafletMouseEvent} from "leaflet";
import {useMemo, useRef} from "react";
import {Marker, Popup, useMapEvents} from "react-leaflet";

export const UnmappedLocationMarker = (props: {
    companyName: string,
    position: LatLng | null,
    setPosition: React.Dispatch<LatLng>
}) => {
    const markerRef = useRef(null)

    const eventHandlers = useMemo(
        () => ({
            dragend() {
                const marker = markerRef.current
                if (marker != null) {
                    // @ts-ignore getLatLng exists on markerRef
                    props.setPosition(marker.getLatLng())
                }
            },
        }),
        [],
    )

    useMapEvents({
        click(e: LeafletMouseEvent) {
            props.setPosition(e.latlng)
        }
    })

    return props.position === null ? null : (
        <Marker ref={markerRef} eventHandlers={eventHandlers} draggable={true} position={props.position}>
            <Popup closeOnClick={false} >{props.companyName}</Popup>
        </Marker>
    )
}
