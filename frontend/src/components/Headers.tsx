import React from "react";
import {useIsAuthenticated} from "@azure/msal-react";
import {SignInButton} from "./SignInButton";
import {SignOutButton} from "./SignOutButton";
import styled from "styled-components";
import {Link} from "react-router-dom";
import {SkillSearchReactAutocomplete} from "./skillSearch/components/SkillSearchReactAutocomplete";

/**
 * Renders the navbar component with a sign-in button if a user is not authenticated
 */
export const Headers = (props: any) => {
    const isAuthenticated = useIsAuthenticated();
    return (
        <Header>
            <HeaderContents>
                <ImageContainer>
                    <Link to="/">
                        <MagelloImage decoding="async"
                                      src="https://magello.se/wp-content/uploads/2022/03/Magello_logo.png"
                                      alt="" width="273" height="70"/>
                    </Link>
                </ImageContainer>
                <NavbarListItem>
                    {isAuthenticated &&
                        <div style={{width: "400px", zIndex: 999}}>
                            <SkillSearchReactAutocomplete/>
                        </div>
                    }
                </NavbarListItem>
                <MagelloNavbar/>
            </HeaderContents>
        </Header>
    );
};

const ImageContainer = styled.div`
  width: 20%;
  height: auto;
`

export const MagelloImage = styled.img`
  width: 160px;
  height: auto;
`

const Header = styled.div`
  font-size: 17px;
  box-sizing: border-box;
  background-color: #fff;
  padding: 0 6%;
  transition: background .3s, border .3s, border-radius .3s, box-shadow .3s;
  z-index: 2;
  display: block;
`

export const HeaderContents = styled.div`
  height: 80px;
  max-width: 1300px;
  align-items: center;
  display: flex;
  margin-right: auto;
  margin-left: auto;
`

export const MagelloNavbar = () => {
    const isAuthenticated = useIsAuthenticated();

    return (
        <NavbarContent>
            <NavbarList>
                <NavbarListItem>
                    <NavbarLink to="/">Karta över var vi jobbar</NavbarLink>
                </NavbarListItem>
                <NavbarListItem>
                    <NavbarLink to="/workplaces">Var vi jobbar</NavbarLink>
                </NavbarListItem>
                <NavbarListItem>
                    {isAuthenticated &&
                        <NavbarLink to="/profile">Din profil</NavbarLink>
                    }
                </NavbarListItem>
                <NavbarListItem>
                    {isAuthenticated ? <SignOutButton/> : <SignInButton/>}
                </NavbarListItem>
            </NavbarList>
        </NavbarContent>
    )
}


export const NavbarList = styled.ul`
  padding-left: 0;
  margin-bottom: 0;
  list-style-type: none;
`

export const NavbarListItem = styled.li`
  list-style: none;
  display: flex;
  box-sizing: border-box;
  float: left;
  position: relative;

  ::before {
    box-sizing: border-box;
  }

  ::after {
    box-sizing: border-box;
  }
`

export const NavbarContent = styled.div`
  width: 80%;
  display: flex;
  margin-left: auto;
  justify-content: flex-end;
  flex-wrap: wrap;
  line-height: normal;
  position: relative;
`

export const NavbarLink = styled(Link)`
  align-self: initial;
  color: #000;
  fill: #000;
  padding: 8px 20px;
  font-size: 18px;
  font-weight: 300;
  line-height: 20px;
  text-decoration: none;
  box-shadow: none;
  box-sizing: border-box;

  :hover {
    color: #8e8e8e;
    fill: #8e8e8e;
  }
`
