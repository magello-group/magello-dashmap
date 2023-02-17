import React, {useCallback, useEffect, useState} from "react";
import {ReactSearchAutocomplete} from "react-search-autocomplete";
import {loginRequest} from "../../../authConfig";
import {AuthenticationResult} from "@azure/msal-browser";
import {useMsal} from "@azure/msal-react";
import {MagelloSkill} from "../../dataTypes/dataTypes";
import {useNavigate} from "react-router-dom";

export const SkillSearchReactAutocomplete = () => {
    let {instance, accounts} = useMsal();
    const navigate = useNavigate();
    const [token, setToken] = useState<string | null>(null)

    useEffect(() => {
        const request = {
            ...loginRequest,
            account: accounts[0]
        };

        // Silently acquires an access token which is then attached to a request for Microsoft Graph data
        instance.acquireTokenSilent(request).then((response: AuthenticationResult) => {
            setToken(response.accessToken);
        }).catch((e) => {
            return instance.acquireTokenPopup(request).then((response: AuthenticationResult) => {
                setToken(response.accessToken);
            });
        });
    }, [setToken, accounts, instance])

    const fetchItems: (keyword: string) => Promise<ListItem[]> = useCallback((keyword: string) => {
        if (!keyword || keyword.length === 0) {
            return Promise.resolve([])
        }
        if (!token) {
            return Promise.resolve([])
        }

        return fetch("http://localhost:8080/skill/search?" + new URLSearchParams({
            query: keyword,
        }), {
            method: 'GET',
            headers: {
                "Accept": "application/json",
                "Authorization": `Bearer ${token}`
            }
        }).then((response) => response.json().then((result) => {
            const resp = result as MagelloSkill[]
            return resp.map((value) => {
                return {
                    id: value.id,
                    name: value.masterSynonym,
                    extras: value.synonyms
                } as ListItem;
            });
        }))
    }, [token])

    const onSelect = useCallback((value: ListItem) => {
        navigate(`skill/${value.id}`)
    }, [navigate])

    return (
        <ReactSearchAutocomplete
            placeholder="Sök efter kompetens inom företaget..."
            fetchNewItems={fetchItems}
            items={[]}
            onSelect={onSelect}
        />
    )
}

interface ListItem {
    id: number,
    name: string,
    extras: string[]
}
