# Google OAuth2 - postup přihlášení / registrace

Tento dokument popisuje princip, jak funguje přihlášení a registrace uživatelů pomocí Google OAuth2 v aplikaci RoboCupMS. 

> [!NOTE]
>Ukázka popisuje manuální postup, za pomocí volání requestů pomocí nástroje __curl__.

## Krok 1: Získání autorizační URL

Zavoláme backend API, které nám vygeneruje URL pro přihlášení k Googlu.

> [!NOTE]
> Požadavek musí být volán z frontendu, který má důvěryhodný. Možné využít Nginx jako reverzní proxy a interně frontend a backend komunikují bez SSL (možné deaktivovat ssl na straně backendu).

```bash
# Flag -k je nutný kvůli self-signed certifikátu
curl -k "https://localhost:8080/auth/oAuth2?redirectURI=https://localhost/auth/oauth2/code"
```

__Odpověď:__ Dostaneme JSON s URL, na kterou přesměrujeme uživatele. Parametr __data_ obsahuje URL, na kterou má byt uživatel přesměrován.

```json
{"type":"RESPONSE","data":"https://accounts.google.com/o/oauth2/v2/auth?scope=https://www.googleapis.com/auth/userinfo.profile%20https://www.googleapis.com/auth/userinfo.email&access_type=offline&include_granted_scopes=true&response_type=code&state=https://localhost/auth/oauth2/code&redirect_uri=https://localhost/auth/oauth2/code&client_id=683711281117-16qb3j3eabdp6vebf9lu3sbdaa0no8hb.apps.googleusercontent.com"}
```

## Krok 2: Přihlášení v prohlížeči

Přihlásíme se k Google účtu a potvrdíme souhlas s oprávněními. Po úspěšném přihlášení bude uživatel přesměrován na URL, kterou jsme zadali v parametru __redirectURI__ (v našem případě `https://localhost/auth/oauth2/code`). V URL bude přidán parametr __code__, který je potřeba pro další krok.

> [!NOTE]
> Pro účely testování jsou povoleny pouze tyto redirect URI:
> * https://localhost/auth/oauth2/code
> * http://localhost/auth/oauth2/code
> * https://localhost:3000/auth/oauth2/code
> * http://localhost:3000/auth/oauth2/code
> * https://robogames.uksouth.cloudapp.azure.com/auth/oauth2/code

## Krok 3: Zpracování přesměrování (Redirect)

1. Google nás přesměruje zpět na frontend, na adresu specifikovanou v parametru __redirectURI__. 

__Přiklad konkretního redirect URL:__
```
https://localhost/auth/oauth2/code?
state=https%3A%2F%2Flocalhost%2Fauth%2Foauth2%2Fcode
&code=4%2F0Ab32j90yFi_iKk2xKykUvte8Goz1R4aKCTbuXyv8AZc52gN6j8FtQnh-iYRrpvUowvEG3Q
&scope=email+profile+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email+openid
&authuser=0
&prompt=consent
```

2. Frontend extrahuje z URL parametr __code__ a __state__ a zavolá backend API pro dokončení přihlášení / registrace.

```bash
curl -k -X POST "https://localhost:8080/auth/oAuth2GenerateToken" \
     -d "redirectURI=<TA_REDIRECT_URI_ZE_STATE>" \
     -d "code=<TEN_KOD>"
```

__Odpověď:__ Dostaneme JSON s autorizačním bearer tokenem, který budeme používat pro autorizaci dalších požadavků.

```json
{"type":"RESPONSE","data":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIyIiwidXNlcm5hbWUiOiJKb2huIERvZSIsImlhdCI6MTY4ODUwMjI3NSwiZXhwIjoxNjg4NTg4Njc1fQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"}
```
