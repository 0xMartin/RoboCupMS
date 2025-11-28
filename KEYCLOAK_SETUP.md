# Keycloak Setup Guide pro RoboCupMS

## Rychlý start

1. **Spusťte celý stack:**
   ```bash
   docker-compose up -d
   ```

2. **Přístup k aplikacím:**
   - **Keycloak Admin Console**: http://localhost:8180
   - **RoboCupMS Backend**: https://localhost:8080
   - **MariaDB**: localhost:3306

## Konfigurace Keycloak

### 1. Přihlášení do Admin Console

- URL: http://localhost:8180
- Username: `admin` (nebo hodnota z `KEYCLOAK_ADMIN_USER`)
- Password: `admin` (nebo hodnota z `KEYCLOAK_ADMIN_PASSWORD`)

### 2. Vytvoření Realm

1. V levém horním rohu klikněte na dropdown "Master"
2. Klikněte na "Create Realm"
3. Název: `RoboCupRealm`
4. Klikněte "Create"

### 3. Vytvoření Client pro Backend

1. V menu vlevo vyberte **Clients**
2. Klikněte "Create client"
3. Nastavte:
   - **Client ID**: `robocup-backend`
   - **Client Protocol**: `openid-connect`
4. Klikněte "Next"
5. Zapněte:
   - **Client authentication**: ON
   - **Authorization**: ON
   - **Standard flow**: ON
   - **Direct access grants**: ON
6. Klikněte "Next"
7. Nastavte:
   - **Valid redirect URIs**: 
     - `https://localhost:8080/*`
     - `http://localhost:8080/*`
   - **Web origins**: 
     - `https://localhost:8080`
     - `http://localhost:8080`
8. Klikněte "Save"

### 4. Získání Client Secret

1. Otevřete vytvořený client `robocup-backend`
2. Přejděte na záložku **Credentials**
3. Zkopírujte **Client Secret**
4. Uložte ho do `.env` souboru jako `KEYCLOAK_CLIENT_SECRET`

### 5. Vytvoření Testovacího Uživatele

1. V menu vlevo vyberte **Users**
2. Klikněte "Add user"
3. Nastavte:
   - **Username**: testuser
   - **Email**: test@example.com
   - **Email verified**: ON
   - **First name**: Test
   - **Last name**: User
4. Klikněte "Create"
5. Přejděte na záložku **Credentials**
6. Klikněte "Set password"
7. Nastavte heslo a vypněte "Temporary"
8. Klikněte "Save"

### 6. Konfigurace Claims (Volitelné)

Pro přidání custom claims jako `surname`:

1. Otevřete Realm `RoboCupRealm`
2. V menu vlevo vyberte **Client scopes**
3. Vyberte `profile`
4. Přejděte na záložku **Mappers**
5. Klikněte "Add mapper" → "By configuration"
6. Vyberte "User Attribute"
7. Nastavte:
   - **Name**: surname
   - **User Attribute**: lastName
   - **Token Claim Name**: surname
   - **Claim JSON Type**: String
   - **Add to ID token**: ON
   - **Add to access token**: ON
   - **Add to userinfo**: ON

### 7. Konfigurace Google OAuth2 (Volitelné)

1. V menu vlevo vyberte **Identity Providers**
2. Vyberte **Google**
3. Nastavte:
   - **Client ID**: Váš Google Client ID
   - **Client Secret**: Váš Google Client Secret
   - **Open ID configuration URL:** https://accounts.google.com/.well-known/openid-configuration
   - **Default Scopes**: `openid profile email`
4. Klikněte "Save"

## Testování Autentizace

### Získání Access Tokenu

```bash
curl -X POST "http://localhost:8180/realms/RoboCupRealm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=robocup-backend" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "username=testuser" \
  -d "password=testpassword" \
  -d "grant_type=password"
```

### Použití Tokenu v API Requestech

```bash
curl -X GET "https://localhost:8080/api/competition/all" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -k
```

## Workflow Autentizace

1. **Uživatel se přihlásí** do Keycloaku
2. **Keycloak vydá JWT token** s user claims
3. **Client pošle request** na backend s Bearer tokenem
4. **Backend validuje token** pomocí Keycloak public key
5. **KeycloakJwtConverter** extrahuje user info z tokenu
6. **Backend synchronizuje** nebo vytvoří uživatele v DB (UserRC)
7. **Request je zpracován** s autentizovaným uživatelem

## Důležité poznámky

- Keycloak běží na portu **8180** (konfigurovatelné přes `KEYCLOAK_PORT`)
- Backend komunikuje s Keycloakem přes interní Docker síť `robocup-network`
- V produkci nastavte `KC_HOSTNAME_STRICT=true` a použijte HTTPS
- Client secret uchovávejte v bezpečí, nikdy ho necommitujte do Gitu

## Troubleshooting

### Keycloak se nespustí
```bash
docker-compose logs keycloak
```

### Backend nemůže validovat tokeny
- Zkontrolujte `KEYCLOAK_AUTH_SERVER_URL` v `.env`
- Ujistěte se, že Realm name je správný
- Ověřte, že Keycloak je healthy: `docker-compose ps`

### JWT token validation fails
- Zkontrolujte issuer URI v `application.properties`
- Ověřte, že client secret je správný
- Zkontrolujte logy: `docker-compose logs app`

## Užitečné odkazy

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
