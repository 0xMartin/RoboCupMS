package com.robogames.RoboCupMS.Controller;

import java.util.List;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Response;
import com.robogames.RoboCupMS.ResponseHandler;
import com.robogames.RoboCupMS.Business.Object.TeamObj;
import com.robogames.RoboCupMS.Business.Service.TeamService;
import com.robogames.RoboCupMS.Entity.Team;

import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping(GlobalConfig.API_PREFIX + "/team")
public class TeamControler {

    @Autowired
    private TeamService teamService;

    /**
     * Navrati info o tymu, ve kterem se prihlaseny uzivatel nachazi
     * 
     * @return Tým, ve kterem se uzivatel nachazi
     */
    @GetMapping("/myTeam")
    Response myTeam() {
        Team team;
        try {
            team = this.teamService.myTeam();
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(team);
    }

    /**
     * Navrati info o tymu s konkretnim ID
     * 
     * @param id ID tymu
     * 
     * @return Hledany tym
     */
    @GetMapping("/findByID")
    Response findID(@RequestParam Long id) {
        Team team;
        try {
            team = this.teamService.findID(id);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(team);
    }

    /**
     * Navrati info o tymu s konkretnim jmenem
     * 
     * @param name Jmeno tymu
     * @return Hledany tym
     */
    @GetMapping("/findByName")
    Response findName(@RequestParam String name) {
        Team team;
        try {
            team = this.teamService.findName(name);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(team);
    }

    /**
     * Navrati vsechny tymy
     * 
     * @return Seznam vsech tymu
     */
    @GetMapping("/all")
    Response getAll() {
        List<Team> all = this.teamService.getAll();
        return ResponseHandler.response(all);
    }

    /**
     * Vytvori novy tym. Uzivatel, ktery tym vytvari se stava jeho vedoucim.
     * 
     * @param teamObj Parametry noveho tymu 
     * @return Informace o stavu provedeneho requestu
     */
    @PostMapping("/create")
    Response create(@RequestBody TeamObj teamObj) {
        try {
            this.teamService.create(teamObj);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Odstrani tym z databaze
     * 
     * @return Informace o stavu provedeneho requestu
     */
    @DeleteMapping("/remove")
    Response remove() {
        try {
            this.teamService.remove();
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Prejmenuje tym
     * 
     * @param name Nove jmeno tymu
     * @return Informace o stavu provedeneho requestu
     */
    @PutMapping("/rename")
    Response rename(@RequestParam String name) {
        try {
            this.teamService.rename(name);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Prida do tymu noveho clena
     * 
     * @param id ID clena, ktery ma byt pridat do tymu
     * @return Informace o stavu provedeneho requestu
     */
    @PutMapping("/addMember")
    Response addMember(@RequestParam Long id) {
        try {
            this.teamService.addMember(id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Odebere z tymu jednoho clena
     * 
     * @param id ID clena, ktery ma byt odebran z tymu
     * @return Informace o stavu provedeneho requestu
     */
    @PutMapping("/removeMember")
    Response removeMember(@RequestParam Long id) {
        try {
            this.teamService.removeMember(id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    @PutMapping("/acceptInvitation")
    Response acceptInvitation(@RequestParam Long id) {
        try {
            this.teamService.acceptInvitation(id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    @PutMapping("/rejectInvitation")
    Response rejectInvitation(@RequestParam Long id) {
        try {
            this.teamService.rejectInvitation(id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Opusti tym, ve ktrem se prihlaseny uzivatel aktualne nachazi
     * 
     * @return Informace o stavu provedeneho requestu
     */
    @PutMapping("/leave")
    Response leave() {
        try {
            this.teamService.leaveTeam();
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

}
