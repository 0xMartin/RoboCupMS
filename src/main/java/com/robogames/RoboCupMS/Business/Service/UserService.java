package com.robogames.RoboCupMS.Business.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Business.Object.UserEditObj;
import com.robogames.RoboCupMS.Business.Security.RegistrationObj;
import com.robogames.RoboCupMS.Entity.Role;
import com.robogames.RoboCupMS.Entity.UserRC;
import com.robogames.RoboCupMS.Repository.RoleRepository;
import com.robogames.RoboCupMS.Repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Zajistuje spravu uzivatelu v systemu
 */
@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Navrati info o prihlasenem uzivateli
     * 
     * @return Informace o uzivateli
     */
    public UserRC getInfo() {
        UserRC user = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user;
    }

    /**
     * Navrati vsechny uzivatele
     * 
     * @return Vsichni uzivatele v databazi
     */
    public List<UserRC> getAll() {
        return repository.findAll();
    }

    /**
     * Navrati jednoho uzivatele se specifickym id
     * 
     * @param id ID hledaneho uzivatele
     * @return Informace o uzivateli
     * @throws Exception
     */
    public UserRC getByID(Long id) throws Exception {
        Optional<UserRC> user = repository.findById(id);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new Exception(String.format("failure, user with ID [%d] not found", id));
        }
    }

    /**
     * Navrati jednoho uzivatele se specifickym emailem
     * 
     * @param id ID hledaneho uzivatele
     * @return Informace o uzivateli
     * @throws Exception
     */
    public UserRC getByEmail(String email) throws Exception {
        Optional<UserRC> user = repository.findByEmail(email);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new Exception(String.format("failure, user with email adress [%s] not found", email));
        }
    }

    /**
     * Prida do databaze noveho uzivatele
     * 
     * @param reg Registracni udaje noveho uzivatele
     * @throws Exception
     */
    public void add(RegistrationObj reg) throws Exception {
        // validace emailu
        // https://mailtrap.io/blog/java-email-validation/
        Pattern pattern = Pattern
                .compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
        if (!pattern.matcher(reg.getEmail()).matches()) {
            throw new Exception("failure, email is invalid");
        }

        // vytvori noveho uzivatele
        List<ERole> roles = new ArrayList<ERole>();
        roles.add(ERole.COMPETITOR);
        UserRC user = new UserRC(
                reg.getName(),
                reg.getSurname(),
                reg.getEmail(),
                reg.getPassword(),
                reg.getBirthDate(),
                roles);

        // uzivatel neni ve vekovem rozsahu definovanem v konfiguraci
        if(user.getBirthDate() == null) {
            throw new Exception("failure, wrong age");
        }

        this.repository.save(user);
    }

    /**
     * Editace udaju prihlaseneho uzivatele
     * 
     * @param userEditObj Nove paramtery uzivatele
     * @throws Exception
     */
    public void edit(UserEditObj userEditObj) throws Exception {
        UserRC user = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        user.setName(userEditObj.getName());
        user.setSurname(userEditObj.getSurname());
        user.setBirthDate(userEditObj.getBirthDate());
        this.repository.save(user);
    }

    /**
     * Zmena uzivatelskeho hesla
     * 
     * @param currentPassword Aktualni heslo
     * @param newPasword      Nove heslo
     * @throws Exception
     */
    public void changePassword(String currentPassword, String newPassword) throws Exception {
        UserRC user = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.passwordMatch(currentPassword)) {
            user.setPassword(newPassword);
            this.repository.save(user);
        } else {
            throw new Exception("failure, your current password is invalid");
        }
    }

    /**
     * Nastavi uzivateli nove heslo
     * 
     * @param newPasword Nove heslo
     * @param id         ID uzivatele, pro ktereho chceme heslo vygenerovat
     * @throws Exception
     */
    public void setPassword(String newPassword, long id) throws Exception {
        Optional<UserRC> user = repository.findById(id);
        if (user.isPresent()) {
            user.get().setPassword(newPassword);
            this.repository.save(user.get());
        } else {
            throw new Exception(String.format("failure, user with ID [%d] not found", id));
        }
    }

    /**
     * Priradi roli uzivateli
     * 
     * @param role Nova role, kterou prideli uzivateli
     * @param id   ID uzivatele
     * @throws Exception
     */
    public void addRole(ERole role, long id) throws Exception {
        // overi zda role existuje
        Optional<Role> newRole = this.roleRepository.findByName(role);
        if (!newRole.isPresent()) {
            throw new Exception(String.format("failure, role [%s] not exists", role.toString()));
        }

        // provede zmeny
        Optional<UserRC> map = repository.findById(id)
                .map(user -> {
                    Set<Role> roles = user.getRoles();
                    if (!roles.contains(newRole.get())) {
                        roles.add(newRole.get());
                    }
                    return repository.save(user);
                });
        if (!map.isPresent()) {
            throw new Exception(String.format("failure, user with ID [%d] not found", id));
        }
    }

    /**
     * Odebere uzivateli zvolenou roli
     * 
     * @param role Nova role, kterou prideli uzivateli
     * @param id   ID uzivatele jehoz atributy budou zmeneny
     * @throws Exception
     */
    public void removeRole(ERole role, long id) throws Exception {
        // overi zda role existuje
        Optional<Role> newRole = this.roleRepository.findByName(role);
        if (!newRole.isPresent()) {
            throw new Exception(String.format("failure, role [%s] not exists", role.toString()));
        }

        // provede zmeny
        Optional<UserRC> map = repository.findById(id)
                .map(user -> {
                    Set<Role> roles = user.getRoles();
                    if (roles.contains(newRole.get())) {
                        roles.remove(newRole.get());
                    }
                    return repository.save(user);
                });
        if (!map.isPresent()) {
            throw new Exception(String.format("failure, user with ID [%d] not found", id));
        }
    }

    /**
     * Odebere uzivatele
     * 
     * @param id ID uzivatele, ktery ma byt odebran
     * @throws Exception
     */
    public void remove(long id) throws Exception {
        Optional<UserRC> user = repository.findById(id);
        if (user.isPresent()) {
            this.repository.delete(user.get());
        } else {
            throw new Exception(String.format("failure, user with ID [%d] not found", id));
        }
    }

}
