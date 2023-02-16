import React, {createContext, useEffect, useState} from 'react';
import './App.css';
import {Headers} from "./components/Headers";
import {MapView} from "./components/map/MapView";
import {ProfilePage} from "./components/profile/ProfilePage";
import {WhereWeWork} from "./components/companyList/WhereWeWork";
import {createBrowserRouter, Outlet, RouterProvider} from "react-router-dom";
import {MagelloWorkAssignment} from "./components/dataTypes/dataTypes";
import {SkillPage} from "./components/skillSearch/SkillPage";

const router = createBrowserRouter([
    {
        path: "/",
        element: <Root/>,
        children: [
            {
                path: "profile",
                element: <ProfilePage/>
            },
            {
                path: "workplaces",
                element: <WhereWeWork/>
            },
            {
                path: "skill/:skillId",
                element: <SkillPage/>
            },
            {
                path: "",
                element: <MapView/>
            }
        ]
    }
])

export const WorkplaceContext = createContext<{workplaces: MagelloWorkAssignment[] | null, isLoading: boolean}>({workplaces: [], isLoading: true});

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
    }, [setData, setIsLoading])

    return (
        <WorkplaceContext.Provider value={{workplaces: data, isLoading: isLoading}}>
            <RouterProvider router={router}/>
        </WorkplaceContext.Provider>
    );
}

function Root() {
    return (
        <>
            <Headers/>
            <Outlet/>
        </>
    )
}

export default App;
