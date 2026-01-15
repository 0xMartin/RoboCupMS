package com.robogames.RoboCupMS.Controller;

import java.util.ArrayList;
import java.util.List;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Response;
import com.robogames.RoboCupMS.ResponseHandler;
import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Business.Object.TeamJoinRequestObj;
import com.robogames.RoboCupMS.Business.Object.TeamObj;
import com.robogames.RoboCupMS.Business.Service.TeamService;
import com.robogames.RoboCupMS.Entity.Team;
import com.robogames.RoboCupMS.Entity.TeamJoinRequest;

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
    @Secured({ ERole.Names.ADMIN })
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

    /**
     * Zmeni vedouciho tymu na jineho clena tymu
     * 
     * @param id ID noveho vedouciho
     * @return Informace o stavu provedeneho requestu
     */
    @PutMapping("/changeLeader")
    Response changeLeader(@RequestParam Long id) {
        try {
            this.teamService.changeLeader(id);
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

    // =====================================================
    // ENDPOINTY PRO POZVÁNÍ DO TYMU (INVITATIONS)
    // =====================================================

    /**
     * Prida do tymu noveho clena podle emailu
     * 
     * @param email Email clena, ktery ma byt pridat do tymu
     * @return Informace o stavu provedeneho requestu
     */
    @PutMapping("/addMemberByEmail")
    Response addMemberByEmail(@RequestParam String email) {
        try {
            this.teamService.addMemberByEmail(email);
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

    // =====================================================
    // ENDPOINTY PRO ZADOSTI O VSTUP DO TYMU (JOIN REQUESTS)
    // =====================================================

    /**
     * Navrati seznam vsech tymu (pouze nazvy a ID) pro uzivatele bez tymu
     * 
     * @return Seznam tymu s nazvy a ID
     */
    @GetMapping("/allNames")
    Response getAllTeamNames() {
        return ResponseHandler.response(this.teamService.getAllTeamNames());
    }

    /**
     * Odesle zadost o vstup do tymu
     * 
     * @param teamId ID tymu
     * @return Informace o stavu provedeneho requestu
     */
    @PostMapping("/joinRequest")
    Response sendJoinRequest(@RequestParam Long teamId) {
        try {
            this.teamService.sendJoinRequest(teamId);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Navrati vsechny zadosti o vstup do tymu pro vedouciho tymu
     * 
     * @return Seznam zadosti
     */
    @GetMapping("/joinRequests")
    Response getJoinRequests() {
        try {
            List<TeamJoinRequestObj> all = new ArrayList<TeamJoinRequestObj>();
            for (TeamJoinRequest req : this.teamService.getJoinRequests()) {
                all.add(new TeamJoinRequestObj(
                    req.getId(),
                    req.getUser().getID(),
                    req.getUser().getName(),
                    req.getUser().getSurname(),
                    req.getUser().getEmail(),
                    req.getTeam().getID(),
                    req.getTeam().getName(),
                    req.getCreatedAt()
                ));
            }
            return ResponseHandler.response(all);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Prijme zadost o vstup do tymu
     * 
     * @param id ID zadosti
     * @return Informace o stavu provedeneho requestu
     */
    @PutMapping("/acceptJoinRequest")
    Response acceptJoinRequest(@RequestParam Long id) {
        try {
            this.teamService.acceptJoinRequest(id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Odmitne zadost o vstup do tymu
     * 
     * @param id ID zadosti
     * @return Informace o stavu provedeneho requestu
     */
    @PutMapping("/rejectJoinRequest")
    Response rejectJoinRequest(@RequestParam Long id) {
        try {
            this.teamService.rejectJoinRequest(id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Zrusi vlastni zadost o vstup do tymu
     * 
     * @param id ID zadosti
     * @return Informace o stavu provedeneho requestu
     */
    @DeleteMapping("/cancelJoinRequest")
    Response cancelJoinRequest(@RequestParam Long id) {
        try {
            this.teamService.cancelJoinRequest(id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Navrati vsechny zadosti odeslane prihlasenym uzivatelem
     * 
     * @return Seznam zadosti
     */
    @GetMapping("/myJoinRequests")
    Response getMyJoinRequests() {
        List<TeamJoinRequestObj> all = new ArrayList<TeamJoinRequestObj>();
        for (TeamJoinRequest req : this.teamService.getMyJoinRequests()) {
            all.add(new TeamJoinRequestObj(
                req.getId(),
                req.getUser().getID(),
                req.getUser().getName(),
                req.getUser().getSurname(),
                req.getUser().getEmail(),
                req.getTeam().getID(),
                req.getTeam().getName(),
                req.getCreatedAt()
            ));
        }
        return ResponseHandler.response(all);
    }

}
