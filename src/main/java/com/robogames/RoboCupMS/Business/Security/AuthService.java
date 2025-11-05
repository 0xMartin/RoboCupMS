package com.robogames.RoboCupMS.Business.Security;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Entity.UserRC;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Zajistuje autentizaci a registraci uzivatelu
 */
@Service
public class AuthService extends OAuth2Service {

    /**
     * Prihlaseni uzivatele do systemu (pokud je email a heslo spravne tak
     * vygeneruje, navrati a zapise do databaze pristupovy token pro tohoto
     * uzivatele)
     * 
     * @param email    Email uzivatele
     * @param password Heslo uzivatele
     * @return Pristupovy token
     */
    public String login(LoginObj login) throws Exception {
        // validace emailu
        // https://mailtrap.io/blog/java-email-validation/
        Pattern pattern = Pattern
                .compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
        if (!pattern.matcher(login.getEmail()).matches()) {
            throw new Exception("failure, email is invalid");
        }

        // autentizace uzivatele
        Optional<UserRC> user = repository.findByEmail(login.getEmail());
        if (user.isPresent()) {
            if (user.get().passwordMatch(login.getPassword())) {
                return TokenAuthorization.generateAccessTokenForUser(user.get(), this.repository);
            }
        }

        throw new Exception("Incorrect email or password");
    }

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
    public void register(RegistrationObj reg) throws Exception {
        // overi zda uzivatel s timto email jiz neni registrovany
        if (this.repository.findByEmail(reg.getEmail()).isPresent()) {
            throw new Exception("failure, user with this email already exists");
        }

        // overi delku emailu
        if (reg.getEmail().length() < 8) {
            throw new Exception("failure, email is too short");
        } else if (reg.getEmail().length() > 30) {
            throw new Exception("failure, email is too long");
        }

        // overi delku jmena
        if (reg.getName().length() < 2) {
            throw new Exception("failure, name is too short");
        } else if (reg.getName().length() > 20) {
            throw new Exception("failure, name is too long");
        }

        // overi delku prijmeni
        if (reg.getSurname().length() < 2) {
            throw new Exception("failure, surname is too short");
        } else if (reg.getSurname().length() > 20) {
            throw new Exception("failure, surname is too long");
        }

        // overi delku hesla
        if (reg.getPassword().length() < 8) {
            throw new Exception("failure, password is too short");
        } else if (reg.getSurname().length() > 30) {
            throw new Exception("failure, password is too long");
        }

        // validace emailu
        // https://mailtrap.io/blog/java-email-validation/
        Pattern pattern = Pattern
                .compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
        if (!pattern.matcher(reg.getEmail()).matches()) {
            throw new Exception("failure, email is invalid");
        }

        // registruje noveho uzivatele
        List<ERole> roles = new ArrayList<ERole>();
        roles.add(ERole.COMPETITOR);
        UserRC u = new UserRC(
                reg.getName(),
                reg.getSurname(),
                reg.getEmail(),
                reg.getPassword(),
                reg.getBirthDate(),
                roles);

        // uzivatel neni ve vekovem rozsahu definovanem v konfiguraci
        if(u.getBirthDate() == null) {
            throw new Exception("failure, wrong age");
        }

        repository.save(u);
    }

}
