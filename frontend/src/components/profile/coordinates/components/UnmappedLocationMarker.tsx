import {LatLng, LeafletMouseEvent} from "leaflet";
import {useMemo, useRef} from "react";
import {Marker, Popup, useMapEvents} from "react-leaflet";

interface UnmappedLocationMarkerProps {
    companyName: string;
    position: LatLng | null;
    setPosition: React.Dispatch<LatLng>;
}

export const UnmappedLocationMarker = ({companyName, position, setPosition}: UnmappedLocationMarkerProps) => {
    const markerRef = useRef(null)

    const eventHandlers = useMemo(
        () => ({
            dragend() {
                const marker = markerRef.current
                if (marker != null) {
                    // @ts-ignore getLatLng exists on markerRef
                    setPosition(marker.getLatLng())
                }
            },
        }),
        [setPosition],
    )

    useMapEvents({
        click(e: LeafletMouseEvent) {
            setPosition(e.latlng)
        }
    })

    return position === null ? null : (
        <Marker ref={markerRef} eventHandlers={eventHandlers} draggable={true} position={position}>
            <Popup closeOnClick={false} >{companyName}</Popup>
        </Marker>
    )
}
