package com.robogames.RoboCupMS.Business.Security;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
// import java.util.UUID;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Entity.UserRC;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import com.robogames.RoboCupMS.Repository.UserRepository;

/**
 * Zajistuje autentizaci a registraci uzivatelu
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository repository;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.token-uri}")
    private String tokenUri;

    @Value("${keycloak.user-info-uri}")
    private String userInfoUri;

    @Value("${keycloak.redirect-uri}")
    private String redirectUri;

    @Value("${keycloak.logout-uri}")
    private String logoutUri;

    @Autowired
    JwtDecoder jwtDecoder;

    /**
     * Odhlasi uzivatele ze systemu (odstrani pristupovy token z databaze)
     * 
     * @param email Email uzivatele
     * @return Status
     */
    public void logout() throws Exception {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (user != null) {
                if (user instanceof UserRC) {
                    ((UserRC) user).setToken(null);
                    repository.save(((UserRC) user));
                    return;
                }
            }
        }
        throw new Exception("failure");
    }

    /**
     * Validuje udaje uzivatele pri registraci
     * 
     * @param email     E-mail
     * @param name      Jmeno
     * @param surname   Prijmeni
     * @param birthDate Datum narozeni
     * @throws Exception
     */
    private void validateUserData(String email, String name, String surname, Date birthDate) throws Exception {
        // overi zda uzivatel s timto email jiz neni registrovany
        if (this.repository.findByEmail(email).isPresent()) {
            throw new Exception("failure, user with this email already exists");
        }

        // overi delku emailu
        if (email.length() < GlobalConfig.USER_EMAIL_MIN_LENGTH) {
            throw new Exception("failure, email is too short");
        } else if (email.length() > GlobalConfig.USER_EMAIL_MAX_LENGTH) {
            throw new Exception("failure, email is too long");
        }

        // validace emailu
        // https://mailtrap.io/blog/java-email-validation/
        Pattern pattern = Pattern
                .compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
        if (!pattern.matcher(email).matches()) {
            throw new Exception("failure, email is invalid");
        }

        // overi delku jmena
        if (name.length() < GlobalConfig.USER_NAME_MIN_LENGTH) {
            throw new Exception("failure, name is too short");
        } else if (name.length() > GlobalConfig.USER_NAME_MAX_LENGTH) {
            throw new Exception("failure, name is too long");
        }

        // overi delku prijmeni
        if (surname.length() < GlobalConfig.USER_SURNAME_MIN_LENGTH) {
            throw new Exception("failure, surname is too short");
        } else if (surname.length() > GlobalConfig.USER_SURNAME_MAX_LENGTH) {
            throw new Exception("failure, surname is too long");
        }

        if (birthDate == null) {
            throw new Exception("failure, birth date is missing");
        }
    }

    /**
     * Vymeni kod ziskany z Keycloaku za pristupovy token, vyhodnoti ho a prihlasi
     * nove vytvoreneho nebo stavajiciho uzivatele
     * 
     * @param code Kod ziskany z Keycloaku
     * @return Pristupovy token
     */
    public String exchange(String code) throws Exception {
        if (code == null) {
            throw new Exception("code is missing");
        }

        // create request body
        String form = "grant_type=authorization_code"
                + "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);

        HttpClient client = HttpClient.newHttpClient();

        // create POST request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUri))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Keycloak token exchange failed: " + response.body());
        }

        // parse response JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(response.body());

        // get access_token from token JSON
        JsonNode accessToken = json.get("access_token");
        if (accessToken == null) {
            throw new Exception("No access_token in response");
        }

        // get refresh_token from token JSON (pro pozdejsi odhlaseni z Keycloaku)
        JsonNode refreshTokenNode = json.get("refresh_token");
        String refreshToken = refreshTokenNode != null ? refreshTokenNode.asText() : null;

        // get user profile from Keycloak userinfo endpoint
        String accessTokenValue = accessToken.asText();
        HttpRequest userInfoRequest = HttpRequest.newBuilder()
                .uri(URI.create(userInfoUri))
                .header("Authorization", "Bearer " + accessTokenValue)
                .GET()
                .build();
        
        HttpResponse<String> userInfoResponse = client.send(userInfoRequest, HttpResponse.BodyHandlers.ofString());
        if (userInfoResponse.statusCode() != 200) {
            throw new Exception("Failed to load userinfo from Keycloak");
        }

        JsonNode userInfo = mapper.readTree(userInfoResponse.body());

        // get user info from Keycloak response
        String email = userInfo.get("email").asText(null);
        String name = userInfo.get("given_name").asText(null);
        String surname = userInfo.get("family_name").asText(null);

        if(email == null || name == null || surname == null) {
            throw new Exception("User profile has missing information from Keycloak");
        }

        // prevedeni data narozeni ze String na Date (pokud je uvedeno)
        JsonNode _birthDate = userInfo.get("birthdate");
        Date birthDate = null;
        if (_birthDate != null && !_birthDate.isNull()) {
            LocalDate localDate = LocalDate.parse(_birthDate.asText(null));
            birthDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else {
            throw new Exception("birthdate is missing");
        }

        // vytvoreni uzivatele / login
        Optional<UserRC> user = this.repository.findByEmail(email);
        String user_access_token = "";
        UserRC targetUser = null;

        if (user.isPresent()) {
            targetUser = user.get();
            
            // kontrola zda uzivatel neni zabanovan
            if (targetUser.isBanned()) {
                // ulozime refresh token aby sme mohli uzivatele odhlasit z Keycloaku
                if (refreshToken != null) {
                    targetUser.setKeycloakRefreshToken(refreshToken);
                    this.repository.save(targetUser);
                }
                // odhlasime uzivatele z Keycloaku na pozadi
                logoutFromKeycloak(targetUser);
                throw new Exception("USER_BANNED");
            }
            
            // prihlasi uzivatele -> vygeneruje pristupovy token
            user_access_token = TokenAuthorization.generateAccessTokenForUser(targetUser, this.repository);
        } else {
            // overi udaje noveho uzivatele
            validateUserData(email, name, surname, birthDate);

            // registruje uzivatele
            List<ERole> roles = new ArrayList<ERole>();
            roles.add(ERole.COMPETITOR);

            UserRC newUser = new UserRC(
                    name,
                    surname,
                    email,
                    birthDate,
                    roles);
            if (newUser.getBirthDate() == null) {
                throw new Exception("failure, wrong age");
            }
            this.repository.save(newUser);
            targetUser = newUser;

            // prihlasi nove registrovaneho uzivatel
            user_access_token = TokenAuthorization.generateAccessTokenForUser(targetUser, this.repository);
        }

        // ulozime refresh token pro pozdejsi odhlaseni z Keycloaku
        if (refreshToken != null && targetUser != null) {
            targetUser.setKeycloakRefreshToken(refreshToken);
            this.repository.save(targetUser);
        }

        // navrati pristupovy token
        return user_access_token;
    }

    /**
     * Odhlasi uzivatele z Keycloaku pomoci refresh tokenu
     * Tato metoda je volana automaticky pri expiraci tokenu
     * 
     * @param user Uzivatel k odhlaseni
     */
    public void logoutFromKeycloak(UserRC user) {
        if (user == null || user.getKeycloakRefreshToken() == null) {
            return;
        }

        try {
            String refreshToken = user.getKeycloakRefreshToken();

            // create request body pro Keycloak logout
            String form = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                    + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
                    + "&refresh_token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);

            HttpClient client = HttpClient.newHttpClient();

            // create POST request na Keycloak logout endpoint
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(logoutUri))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 204) {
                System.out.println("User " + user.getEmail() + " successfully logged out from Keycloak");
            } else {
                System.err.println("Keycloak logout failed for user " + user.getEmail() + ": " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Error during Keycloak logout for user " + user.getEmail() + ": " + e.getMessage());
        } finally {
            // vzdy smazeme refresh token z databaze
            user.setKeycloakRefreshToken(null);
            repository.save(user);
        }
    }

}
