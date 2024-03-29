import styled, {css} from "styled-components";

export const FormLabel = styled.label`
  width: 100%;
  padding: 12px 20px;
  margin: 8px auto;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  font-family: inherit;
  font-size: 1rem;
`

export const DefaultFormButton = css`
  padding: 12px 20px;
  display: inline-block;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
  font-family: inherit;
  font-size: 1rem;

  :hover {
    border: 1px #00aeef solid;

    :active {
      background-color: #00aeef;
    }
  }
`

export const FormButton = styled.input`
  ${DefaultFormButton}
`

export const FormInput = styled.input`
  width: 100%;
  padding: 12px 20px;
  margin: 8px auto;
  display: inline-block;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
  font-family: inherit;
  font-size: 1rem;

  :focus-visible {
    outline: 1px #00aeef solid;
  }
`

export const FormTextArea = styled.textarea`
  width: 100%;
  padding: 12px 20px;
  margin: 8px auto;
  display: inline-block;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
  font-family: inherit;
  font-size: 1rem;

  :focus-visible {
    outline: 1px #00aeef solid;
  }
`

export const DividerSolid = styled.hr`
  width: 95%;
  border: 1px solid #ccc;
`
