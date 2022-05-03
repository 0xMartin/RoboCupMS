package com.robogames.RoboCupMS.Controller;

import java.util.List;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Response;
import com.robogames.RoboCupMS.ResponseHandler;
import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Business.Service.UserService;
import com.robogames.RoboCupMS.Entity.UserRC;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(GlobalConfig.API_PREFIX + "/user")
public class UserControler {

    @Autowired
    private UserService userService;

    /**
     * Navrati info o prihlasenem uzivateli
     * 
     * @return Informace o uzivateli
     */
    @GetMapping("/info")
    Response getInfo() {
        UserRC user = this.userService.getInfo();
        return ResponseHandler.response(user);
    }

    /**
     * Navrati vsechny uzivatele
     * 
     * @return Vsichni uzivatele v databazi
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.ASSISTANT })
    @GetMapping("/all")
    Response getAll() {
        List<UserRC> all = this.userService.getAll();
        return ResponseHandler.response(all);
    }

    /**
     * Navrati jednoho uzivatele se specifickym id
     * 
     * @param id ID hledaneho uzivatele
     * @return Informace o uzivateli
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.ASSISTANT })
    @GetMapping("/getByID")
    Response getByID(@RequestParam Long id) {
        UserRC user;
        try {
            user = this.userService.getByID(id);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(user);
    }

    /**
     * Navrati jednoho uzivatele se specifickym emailem
     * 
     * @param id ID hledaneho uzivatele
     * @return Informace o uzivateli
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.ASSISTANT })
    @GetMapping("/getByEmail")
    Response getByEmail(@RequestParam String email) {
        UserRC user;
        try {
            user = this.userService.getByEmail(email);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(user);
    }

    /**
     * Prida do databaze noveho uzivatele
     * 
     * @param newUser Novy uzivatel
     * @return Informace o stavu provedene operace
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER })
    @PostMapping("/add")
    Response add(@RequestBody UserRC newUser) {
        try {
            this.userService.add(newUser);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Editace atributu uzivatele s konktretnim ID
     * 
     * @param newUser Nove atributy uzivatele
     * @param id      ID uzivatele jehoz atributy budou zmeneny
     * @return Informace o stavu provedene operace
     */
    @PutMapping("/edit")
    Response edit(@RequestBody UserRC newUser, @RequestParam String uuid) {
        try {
            this.userService.edit(newUser, uuid);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Zmena uzivatelskeho hesla
     * 
     * @param oldPassword Stare heslo
     * @param newPasword  Nove heslo
     * @return Informace o stavu provedene operace
     */
    @PutMapping("/changePassword")
    Response changePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        try {
            this.userService.changePassword(oldPassword, newPassword);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Vygenerovat nove heslo
     * 
     * @param newPasword Nove heslo
     * @return Informace o stavu provedene operace
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER })
    @PutMapping("/generatePassword")
    Response generatePassword(@RequestParam String newPassword, @RequestParam String uuid) {
        try {
            this.userService.generatePassword(newPassword, uuid);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Prida roli uzivateli
     * 
     * @param role Nova role, kterou prideli uzivateli
     * @param id   ID uzivatele jehoz atributy budou zmeneny
     * @return Informace o stavu provedene operace
     */
    @Secured({ ERole.Names.ADMIN })
    @PutMapping("/addRole")
    Response addRole(@RequestParam ERole role, @RequestParam String uuid) {
        try {
            this.userService.addRole(role, uuid);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Prida roli uzivateli
     * 
     * @param role Nova role, kterou prideli uzivateli
     * @param id   ID uzivatele jehoz atributy budou zmeneny
     * @return Informace o stavu provedene operace
     */
    @Secured({ ERole.Names.ADMIN })
    @PutMapping("/removeRole")
    Response removeRole(@RequestParam ERole role, @RequestParam String uuid) {
        try {
            this.userService.removeRole(role, uuid);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Odebere uzivatele
     * 
     * @param id ID uzivatele, ktery ma byt odebran
     * @return Informace o stavu provedene operace
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER })
    @DeleteMapping("/delete")
    Response delete(@RequestParam String uuid) {
        try {
            this.userService.delete(uuid);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

}