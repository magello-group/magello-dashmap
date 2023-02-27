import React, {createContext, useEffect, useState} from 'react';
import './App.css';
import {Headers} from "./components/Headers";
import {MapView} from "./components/map/MapView";
import {ProfilePage} from "./components/profile/ProfilePage";
import {WhereWeWork} from "./components/companyList/WhereWeWork";
import {createBrowserRouter, Outlet, RouterProvider} from "react-router-dom";
import {MagelloWorkAssignment} from "./components/dataTypes/dataTypes";
import {SkillPage} from "./components/skillSearch/SkillPage";
import {createGlobalStyle} from "styled-components";

const GlobalStyle = createGlobalStyle`
:root {
  --magello-color-blue: #00aeef;
  --magello-color-red: #ed1a3b;
  --magello-color-yellow: #fd0;
  --magello-color-green: #00a65d;
  --magello-color-orange: #fdb913;
  --magello-color-magenta: #ec008c;

  --toastify-color-error: var(--magello-color-red);
  --toastify-color-success: var(--magello-color-green);
}
`

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
                path: "profile/:profileId",
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
            <GlobalStyle/>
            <Headers/>
            <Outlet/>
        </>
    )
}

export default App;
