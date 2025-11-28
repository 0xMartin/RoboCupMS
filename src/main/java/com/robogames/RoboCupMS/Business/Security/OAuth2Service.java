package com.robogames.RoboCupMS.Business.Security;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Entity.UserRC;
import com.robogames.RoboCupMS.Repository.UserRepository;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

// @Service - Temporarily disabled, using Keycloak OAuth2 instead
public class OAuth2Service {

    // @Value("${oauth2.google.client-id}")
    private String client_id;

    // @Value("${oauth2.google.client-secret}")
    private String client_secret;

    // @Autowired
    protected UserRepository repository;

    /**
     * Vygeneruje odkaz pro autorizaci a autentizaci uzivatele. Po uspesne
     * autorizaci je uzivatel presmerovan na specifikovanou adresu. Zde musi byt
     * odeslan POST request na endpoint serveru "/auth/oAuth2GenerateToken" s
     * parametrem "code" a "redirectURI" jejihz hodnuty ziska aktualni URL.
     * "redirectURI" bude predan parametrem "state".
     * 
     * @param redirectURI Presmerovani na URL "frond-end" -> pres JS predat
     *                    autorizacni kod serveru "oAuth2GenerateToken
     * @return URL odkaz pro autorizaci a autentizaci uzivatele
     * @throws Exception
     */
    public String getOAuth2URI(String redirectURI) throws Exception {
        StringBuilder str = new StringBuilder();
        str.append("https://accounts.google.com/o/oauth2/v2/auth?");
        str.append("scope=https://www.googleapis.com/auth/userinfo.profile%20");
        str.append("https://www.googleapis.com/auth/userinfo.email&");
        str.append("access_type=offline&");
        str.append("include_granted_scopes=true&");
        str.append("response_type=code&");
        str.append(String.format("state=%s&", redirectURI));
        str.append(String.format("redirect_uri=%s&", redirectURI));
        str.append(String.format("client_id=%s", this.client_id));
        URI uri = new URI(str.toString());
        return uri.toString();
    }

    /**
     * Vygeneruje pristupovy token pro uzivatele s vyuzitim OAuth2 autorizacniho
     * kodu
     * 
     * @param redirectURI URI presmerovani, zistka paramatru state spolu s
     *                    autorizacnim kodem
     * @param code        Autorizacni kod pro klienta
     * @return Pristupovy token uzivatele
     * @throws Exception
     */
    public String oAuth2GenerateToken(String redirectURI, String code) throws Exception {
        JSONObject response;
        HttpURLConnection http;
        StringBuilder stringBuilder = new StringBuilder();

        // sestavy URI requeste => secrit a code vymeni ze pristupovy token
        stringBuilder.append("https://oauth2.googleapis.com/token?");
        stringBuilder.append(String.format("code=%s&", code));
        stringBuilder.append(String.format("client_id=%s&", this.client_id));
        stringBuilder.append(String.format("client_secret=%s&", this.client_secret));
        stringBuilder.append(String.format("redirect_uri=%s&", redirectURI));
        stringBuilder.append("grant_type=authorization_code");

        // sestaveni requestu
        http = (HttpURLConnection) (new URL(stringBuilder.toString())).openConnection();
        http.setRequestMethod("POST");
        http.setRequestProperty("Host", "oauth2.googleapis.com");
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        http.setDoOutput(true);

        // prazdne data -> zabrani chybe 411 (server zahazuje post req bez dat)
        OutputStreamWriter writers = new OutputStreamWriter(http.getOutputStream());
        writers.write("{}");
        writers.flush();
        writers.close();

        // prijme a zpacuje prichozi data
        stringBuilder.delete(0, stringBuilder.length());
        if (http.getResponseCode() == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();
        } else {
            throw new Exception("failure, exchange code for token failed");
        }
        http.disconnect();
        response = (JSONObject) new JSONParser().parse(stringBuilder.toString());

        // sestavi URI pro request na zistani dat z resource serveru google
        String token_api = (String) response.get("access_token");
        http = (HttpURLConnection) (new URL("https://www.googleapis.com/oauth2/v3/userinfo")).openConnection();
        http.setRequestMethod("GET");
        http.setRequestProperty("Host", "oauth2.googleapis.com");
        http.setRequestProperty("Authorization", String.format("Bearer %s", token_api));

        // ziska data z resource serveru (v tomto pripade google userinfo.profile)
        stringBuilder.delete(0, stringBuilder.length());
        if (http.getResponseCode() == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();
        } else {
            throw new Exception("failure, failed to access on resource server");
        }
        http.disconnect();

        // ze ziskanych dat prihlasy uzivatele do systemu, pokud neexistuje tak ho
        // registruje
        response = (JSONObject) new JSONParser().parse(stringBuilder.toString());
        String name = (String) response.get("given_name");
        String surname = (String) response.get("family_name");
        String email = (String) response.get("email");
        if (name == null || surname == null || email == null) {
            throw new Exception("failure, missing user informations");
        }

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
                    UUID.randomUUID().toString(),
                    new GregorianCalendar(2000, Calendar.JANUARY, 1).getTime(),
                    roles);
            this.repository.save(newUser);
            // prihlasi nove registrovaneho uzivatel
            user_access_token = TokenAuthorization.generateAccessTokenForUser(newUser, this.repository);
        }

        // navrati pristupovy token
        return user_access_token;
    }

}
