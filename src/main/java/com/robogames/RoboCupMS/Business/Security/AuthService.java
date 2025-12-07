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
// import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Entity.UserRC;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
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

    @Value("${keycloak.redirect-uri}")
    private String redirectUri;

    @Autowired
    JwtDecoder jwtDecoder;

    /**
     * Prihlaseni uzivatele do systemu (pokud je email a heslo spravne tak
     * vygeneruje, navrati a zapise do databaze pristupovy token pro tohoto
     * uzivatele)
     * 
     * @param email    Email uzivatele
     * @param password Heslo uzivatele
     * @return Pristupovy token
     */
    // public String login(LoginObj login) throws Exception {
    //     // validace emailu
    //     // https://mailtrap.io/blog/java-email-validation/
    //     Pattern pattern = Pattern
    //             .compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    //     if (!pattern.matcher(login.getEmail()).matches()) {
    //         throw new Exception("failure, email is invalid");
    //     }

    //     // autentizace uzivatele
    //     Optional<UserRC> user = repository.findByEmail(login.getEmail());
    //     if (user.isPresent()) {
    //         if (user.get().passwordMatch(login.getPassword())) {
    //             return TokenAuthorization.generateAccessTokenForUser(user.get(), this.repository);
    //         }
    //     }

    //     throw new Exception("Incorrect email or password");
    // }

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
     * Registruje noveho uzivatele
     * 
     * @param reg Registracni udaje noveho uzivatele
     * @return Nove vytvoreni uzivatel
     */
    // public void register(RegistrationObj reg) throws Exception {
    //     // overi zda uzivatel s timto email jiz neni registrovany
    //     if (this.repository.findByEmail(reg.getEmail()).isPresent()) {
    //         throw new Exception("failure, user with this email already exists");
    //     }

    //     // overi delku emailu
    //     if (reg.getEmail().length() < 8) {
    //         throw new Exception("failure, email is too short");
    //     } else if (reg.getEmail().length() > 30) {
    //         throw new Exception("failure, email is too long");
    //     }

    //     // overi delku jmena
    //     if (reg.getName().length() < 2) {
    //         throw new Exception("failure, name is too short");
    //     } else if (reg.getName().length() > 20) {
    //         throw new Exception("failure, name is too long");
    //     }

    //     // overi delku prijmeni
    //     if (reg.getSurname().length() < 2) {
    //         throw new Exception("failure, surname is too short");
    //     } else if (reg.getSurname().length() > 20) {
    //         throw new Exception("failure, surname is too long");
    //     }

    //     // overi delku hesla
    //     if (reg.getPassword().length() < 8) {
    //         throw new Exception("failure, password is too short");
    //     } else if (reg.getSurname().length() > 30) {
    //         throw new Exception("failure, password is too long");
    //     }

    //     // validace emailu
    //     // https://mailtrap.io/blog/java-email-validation/
    //     Pattern pattern = Pattern
    //             .compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    //     if (!pattern.matcher(reg.getEmail()).matches()) {
    //         throw new Exception("failure, email is invalid");
    //     }

    //     // registruje noveho uzivatele
    //     List<ERole> roles = new ArrayList<ERole>();
    //     roles.add(ERole.COMPETITOR);
    //     UserRC u = new UserRC(
    //             reg.getName(),
    //             reg.getSurname(),
    //             reg.getEmail(),
    //             reg.getPassword(),
    //             reg.getBirthDate(),
    //             roles);

    //     // uzivatel neni ve vekovem rozsahu definovanem v konfiguraci
    //     if (u.getBirthDate() == null) {
    //         throw new Exception("failure, wrong age");
    //     }

    //     repository.save(u);
    // }

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
        JsonNode tokenNode = json.get("access_token");
        if (tokenNode == null) {
            throw new Exception("No access_token in response");
        }

        // get user info from Keycloak
        Jwt jwt = jwtDecoder.decode(tokenNode.asText());
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("given_name");
        String surname = jwt.getClaimAsString("family_name");
        String _birthDate = jwt.getClaimAsString("birthdate");

        // prevedeni data narozeni ze String na Date (pokud je uvedeno)
        Date birthDate = null;
        if (_birthDate != null && !_birthDate.isEmpty()) {
            LocalDate localDate = LocalDate.parse(_birthDate);
            birthDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else {
            // pokud neni birthdate v tokenu, pouzijeme vychozi datum (napr. 2000-01-01)
            birthDate = Date.from(LocalDate.of(2000, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        // vytvoreni uzivatel / login
        Optional<UserRC> user = this.repository.findByEmail(email);
        String user_access_token = "";

        if (user.isPresent()) {
            // prihlasi uzivatele -> vygeneruje pristupovy token
            user_access_token = TokenAuthorization.generateAccessTokenForUser(user.get(), this.repository);
        } else {
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

            // prihlasi nove registrovaneho uzivatel
            user_access_token = TokenAuthorization.generateAccessTokenForUser(newUser, this.repository);
        }

        // navrati pristupovy token
        return user_access_token;
    }

}
