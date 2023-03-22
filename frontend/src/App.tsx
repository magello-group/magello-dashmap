import React, {createContext, useCallback, useEffect, useState} from 'react';
import './App.css';
import {Headers} from "./components/Headers";
import {MapView} from "./components/map/MapView";
import {ProfilePage} from "./components/profile/ProfilePage";
import {WhereWeWork} from "./components/companyList/WhereWeWork";
import {createBrowserRouter, Outlet, RouterProvider} from "react-router-dom";
import {MagelloWorkAssignment} from "./components/dataTypes/dataTypes";
import {SkillPage} from "./components/skillSearch/SkillPage";
import {createGlobalStyle} from "styled-components";
import {toast, ToastContainer} from "react-toastify";

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

export const WorkplaceContext = createContext<{ workplaces: MagelloWorkAssignment[] | null, isLoading: boolean, reload: () => void }>({
    workplaces: [],
    isLoading: true,
    reload: () => {
    }
});

const fetchWorkplaces = () => {
    return fetch(`${process.env.REACT_APP_BACKEND_HOST}/workplaces`, {
        method: 'GET',
        headers: {
            "Accept": "application/json",
        }
    })
}

function App() {
    const [isLoading, setIsLoading] = useState<boolean>(true)
    const [data, setData] = useState<MagelloWorkAssignment[] | null>(null)

    useEffect(() => {
        fetchWorkplaces()
            .then((response) => {
                if (response.status === 200) {
                    response.json().then((body) => {
                        setData(body);
                        setIsLoading(false);
                    })
                } else {
                    toast.error(() => (
                        <div>Hämtning av arbetsplatsinformation fallerade!
                            <p style={{fontSize: "14px", fontWeight: 400}}>Fick status {response.status}, prova att
                                refresha sidan eller fråga Fabian!</p></div>
                    ), {
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
            })
            .catch(() => {
                toast.error(() => (
                    <div>Hämtning av arbetsplatsinformation fallerade!
                        <p style={{fontSize: "14px", fontWeight: 400}}>Prova att refresha sidan eller fråga Fabian!</p></div>
                ), {
                    position: "bottom-center",
                    autoClose: 5000,
                    hideProgressBar: false,
                    closeOnClick: true,
                    pauseOnHover: true,
                    draggable: true,
                    progress: undefined,
                    theme: "light",
                });
            })
    }, [setData, setIsLoading])

    const reload = useCallback(() => {
        setIsLoading(true);
        fetchWorkplaces()
            .then((response) => {
                response.json().then((body) => {
                    setData(body);
                    setIsLoading(false);
                })
            })
    }, [setData, setIsLoading])

    return (
        <WorkplaceContext.Provider value={{workplaces: data, isLoading: isLoading, reload: reload}}>
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
            <ToastContainer
                position="bottom-center"
                autoClose={5000}
                hideProgressBar={false}
                newestOnTop={false}
                closeOnClick
                rtl={false}
                pauseOnFocusLoss
                draggable
                pauseOnHover
                theme="light"
            />
        </>
    )
}

export default App;
