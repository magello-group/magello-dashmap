import React, {useEffect, useState} from 'react';
import './App.css';
import {Headers} from "./components/Headers";
import {Router} from "@reach/router";
import {MapView} from "./components/map/MapView";
import {ProfilePage} from "./components/profile/ProfilePage";
import {MagelloWorkAssignment} from "./components/dataTypes/dataTypes";

function App() {
    const [isLoading, setIsLoading] = useState<boolean>(true)
    const [data, setData] = useState<MagelloWorkAssignment[] | null>(null)

    useEffect(() => {
        fetch("http://localhost:8080/workplaces", {
            method: 'GET',
            headers: {
                "Accept": "application/json",
            }
        }).then((response) => {
            response.json().then((body) => {
                setData(body);
                setIsLoading(false);
            })
        })
    }, [])

    return (
        <>
            <Headers/>
            <Router>
                <MapView path="/" data={data} isLoading={isLoading}/>
                <ProfilePage path="/profile"/>
                {
                    /*
                    <WhereWeWork path="/workplaces" data={data} isLoading={isLoading}/>
                     */
                }
            </Router>
        </>
    );
}

export default App;
