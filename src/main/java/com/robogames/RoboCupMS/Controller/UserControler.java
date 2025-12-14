package com.robogames.RoboCupMS.Controller;

import java.util.ArrayList;
import java.util.List;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Response;
import com.robogames.RoboCupMS.ResponseHandler;
import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Business.Object.TeamInvitationObj;
import com.robogames.RoboCupMS.Business.Object.UserEditObj;
import com.robogames.RoboCupMS.Business.Security.RegistrationObj;
import com.robogames.RoboCupMS.Business.Service.UserService;
import com.robogames.RoboCupMS.Entity.TeamInvitation;
import com.robogames.RoboCupMS.Entity.UserRC;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
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

    @GetMapping("/getTeamInvitations")
    Response getTeamInvitations() {
        List<TeamInvitationObj> all = new ArrayList<TeamInvitationObj>();
        for (TeamInvitation inv : this.userService.getTeamInvitations()) {
            all.add(new TeamInvitationObj(inv.getId(), inv.getUser().getID(), inv.getTeam().getID(),
                    inv.getTeam().getName()));
        }
        return ResponseHandler.response(all);
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

    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.ASSISTANT, ERole.Names.COMPETITOR })
    @GetMapping("/allNoTeam")
    Response getAllNoTeam() {
        List<UserRC> all = this.userService.getAllNoTeam();
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
     * @param newUser Registracni udaje noveho uzivatele
     * @return Informace o stavu provedeneho requestu
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER })
    @PostMapping("/add")
    Response add(@RequestBody RegistrationObj newUser) {
        try {
            this.userService.add(newUser);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Editace udaju prihlaseneho uzivatele
     * 
     * @param userEditObj Nove parametry uzivatele
     * @return Informace o stavu provedeneho requestu
     */
    @PutMapping("/edit")
    Response edit(@RequestBody UserEditObj userEditObj) {
        try {
            this.userService.edit(userEditObj);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Editace udaju vybraneho uzivatele
     * 
     * @param userEditObj Nove parametry uzivatele
     * @param id Id uzivatele
     * @return Informace o stavu provedeneho requestu
     */
    @Secured({ ERole.Names.ADMIN })
    @PutMapping("/editById")
    Response editById(@RequestBody UserEditObj userEditObj, @RequestParam long id) {
        try {
            this.userService.editById(userEditObj, id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Priradi roli uzivateli
     * 
     * @param role Nova role, kterou prideli uzivateli
     * @param id   ID uzivatele jehoz atributy budou zmeneny
     * @return Informace o stavu provedeneho requestu
     */
    @Secured({ ERole.Names.ADMIN })
    @PutMapping("/addRole")
    Response addRole(@RequestParam ERole role, @RequestParam long id) {
        try {
            this.userService.addRole(role, id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Odebere uzivateli zvolenou roli
     * 
     * @param role Nova role, kterou prideli uzivateli
     * @param id   ID uzivatele jehoz atributy budou zmeneny
     * @return Informace o stavu provedeneho requestu
     */
    @Secured({ ERole.Names.ADMIN })
    @PutMapping("/removeRole")
    Response removeRole(@RequestParam ERole role, @RequestParam long id) {
        try {
            this.userService.removeRole(role, id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Odstrani uzivatele z databaze
     * 
     * @param id ID uzivatele, ktery ma byt odebran
     * @return Informace o stavu provedeneho requestu
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER })
    @DeleteMapping("/remove")
    Response remove(@RequestParam long id) {
        try {
            this.userService.remove(id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

}
