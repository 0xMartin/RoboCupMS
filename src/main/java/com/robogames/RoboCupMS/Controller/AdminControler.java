package com.robogames.RoboCupMS.Controller;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Response;
import com.robogames.RoboCupMS.ResponseHandler;
import com.robogames.RoboCupMS.Business.Enum.ECategory;
import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Business.Object.AdminRobotCreateObj;
import com.robogames.RoboCupMS.Business.Object.AdminRobotEditObj;
import com.robogames.RoboCupMS.Business.Object.AdminTeacherEditObj;
import com.robogames.RoboCupMS.Business.Object.AdminTeamCreateObj;
import com.robogames.RoboCupMS.Business.Object.AdminTeamEditObj;
import com.robogames.RoboCupMS.Business.Object.AdminTeamRegistrationObj;
import com.robogames.RoboCupMS.Business.Service.AdminService;
import com.robogames.RoboCupMS.Entity.Robot;
import com.robogames.RoboCupMS.Entity.Team;
import com.robogames.RoboCupMS.Entity.TeamRegistration;

import org.springframework.security.access.annotation.Secured;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin kontroler pro administrativni operace.
 * Umoznuje adminovi menit data v aplikaci nezavisle na beznych omezenich.
 * Vsechny endpointy vyzaduji roli ADMIN.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(GlobalConfig.API_PREFIX + "/admin")
@Secured({ ERole.Names.ADMIN })
@Transactional
public class AdminControler {

    @Autowired
    private AdminService adminService;

    // ==================== TEAM OPERATIONS ==========================================================
    // ===============================================================================================

    /**
     * Vytvori novy tym
     * Admin muze specifikovat vedouciho a cleny tymu.
     * Pokud je uzivatel v jinem tymu, bude z nej odebran.
     * 
     * @param teamCreateObj Parametry noveho tymu (name, leaderId, memberIds)
     * @return Vytvoreny tym
     */
    @PostMapping("/team/create")
    Response createTeam(@RequestBody AdminTeamCreateObj teamCreateObj) {
        try {
            Team team = this.adminService.createTeam(teamCreateObj);
            return ResponseHandler.response(team);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Odstrani tym
     * Tym nelze odstranit pokud ma potvrzene roboty (zachovani dat pro soutez).
     * 
     * @param id ID tymu
     * @return Informace o stavu provedeneho requestu
     */
    @DeleteMapping("/team/remove")
    Response removeTeam(@RequestParam Long id) {
        try {
            this.adminService.removeTeam(id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Edituje udaje tymu (nazev, vedouci)
     * 
     * @param id      ID tymu
     * @param editObj Nove udaje tymu
     * @return Aktualizovany tym
     */
    @PutMapping("/team/edit")
    Response editTeam(@RequestParam Long id, @RequestBody AdminTeamEditObj editObj) {
        try {
            Team team = this.adminService.editTeam(id, editObj);
            return ResponseHandler.response(team);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Prida uzivatele do tymu
     * Pokud je uzivatel v jinem tymu, bude z nej odebran.
     * 
     * @param teamId ID tymu
     * @param userId ID uzivatele
     * @return Informace o stavu provedeneho requestu
     */
    @PutMapping("/team/addUser")
    Response addUserToTeam(@RequestParam Long teamId, @RequestParam Long userId) {
        try {
            this.adminService.addUserToTeam(teamId, userId);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Odebere uzivatele z tymu
     * 
     * @param teamId ID tymu
     * @param userId ID uzivatele
     * @return Informace o stavu provedeneho requestu
     */
    @PutMapping("/team/removeUser")
    Response removeUserFromTeam(@RequestParam Long teamId, @RequestParam Long userId) {
        try {
            this.adminService.removeUserFromTeam(teamId, userId);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Nastavi noveho vedouciho tymu
     * Uzivatel musi byt clenem tymu.
     * 
     * @param teamId      ID tymu
     * @param newLeaderId ID noveho vedouciho
     * @return Informace o stavu provedeneho requestu
     */
    @PutMapping("/team/setLeader")
    Response setTeamLeader(@RequestParam Long teamId, @RequestParam Long newLeaderId) {
        try {
            this.adminService.setTeamLeader(teamId, newLeaderId);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Presune uzivatele z jednoho tymu do druheho
     * 
     * @param userId    ID uzivatele
     * @param newTeamId ID noveho tymu
     * @return Informace o stavu provedeneho requestu
     */
    @PutMapping("/team/transferUser")
    Response transferUser(@RequestParam Long userId, @RequestParam Long newTeamId) {
        try {
            this.adminService.transferUser(userId, newTeamId);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    // ==================== TEAM REGISTRATION OPERATIONS =============================================
    // ===============================================================================================

    /**
     * Registruje tym do souteze
     * Admin muze registrovat tym kdykoliv, i po zahajeni souteze.
     * 
     * @param registrationObj Parametry registrace (teamId, year, teacherName, teacherSurname, teacherContact)
     * @return Vytvorena registrace
     */
    @PostMapping("/registration/create")
    Response registerTeamToCompetition(@RequestBody AdminTeamRegistrationObj registrationObj) {
        try {
            TeamRegistration registration = this.adminService.registerTeamToCompetition(registrationObj);
            return ResponseHandler.response(registration);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Zrusi registraci tymu ze souteze
     * Registraci nelze zrusit pokud ma potvrzene roboty (zachovani dat).
     * 
     * @param id ID registrace tymu
     * @return Informace o stavu provedeneho requestu
     */
    @DeleteMapping("/registration/remove")
    Response unregisterTeamFromCompetition(@RequestParam Long id) {
        try {
            this.adminService.unregisterTeamFromCompetition(id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Zmeni udaje o uciteli (zodpovedne osobe) v registraci tymu
     * 
     * @param id         ID registrace tymu
     * @param teacherObj Nove udaje o uciteli
     * @return Aktualizovana registrace
     */
    @PutMapping("/registration/editTeacher")
    Response editTeacherInfo(@RequestParam Long id, @RequestBody AdminTeacherEditObj teacherObj) {
        try {
            TeamRegistration registration = this.adminService.editTeacherInfo(id, teacherObj);
            return ResponseHandler.response(registration);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Forcne zmeni kategorii registrace tymu (pro vyjimecne situace)
     * 
     * @param id       ID registrace tymu
     * @param category Nova kategorie (LOW_AGE_CATEGORY nebo HIGH_AGE_CATEGORY)
     * @return Aktualizovana registrace
     */
    @PutMapping("/registration/forceChangeCategory")
    Response forceChangeCategory(@RequestParam Long id, @RequestParam ECategory category) {
        try {
            TeamRegistration registration = this.adminService.forceChangeCategory(id, category);
            return ResponseHandler.response(registration);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    // ==================== ROBOT OPERATIONS =========================================================
    // ===============================================================================================

    /**
     * Vytvori robota na existujici registraci tymu
     * Robot bude ve stavu "nepotvrzeny".
     * 
     * @param robotCreateObj Parametry noveho robota (teamRegistrationId, name, disciplineId)
     * @return Vytvoreny robot
     */
    @PostMapping("/robot/create")
    Response createRobot(@RequestBody AdminRobotCreateObj robotCreateObj) {
        try {
            Robot robot = this.adminService.createRobot(robotCreateObj);
            return ResponseHandler.response(robot);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Odstrani robota z registrace tymu
     * Robota nelze odstranit pokud je potvrzeny (zachovani dat).
     * 
     * @param id ID robota
     * @return Informace o stavu provedeneho requestu
     */
    @DeleteMapping("/robot/remove")
    Response removeRobot(@RequestParam Long id) {
        try {
            this.adminService.removeRobot(id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Edituje udaje robota
     * Lze menit: jmeno, cislo, disciplinu, stav potvrzeni.
     * Nelze menit referenci na registraci tymu.
     * 
     * @param id      ID robota
     * @param editObj Nove udaje robota
     * @return Aktualizovany robot
     */
    @PutMapping("/robot/edit")
    Response editRobot(@RequestParam Long id, @RequestBody AdminRobotEditObj editObj) {
        try {
            Robot robot = this.adminService.editRobot(id, editObj);
            return ResponseHandler.response(robot);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Forcne potvrdi nebo zrusi potvrzeni registrace robota
     * Pro reseni problemu na soutezi.
     * 
     * @param id        ID robota
     * @param confirmed Novy stav potvrzeni
     * @return Aktualizovany robot
     */
    @PutMapping("/robot/forceConfirm")
    Response forceConfirmRobot(@RequestParam Long id, @RequestParam boolean confirmed) {
        try {
            Robot robot = this.adminService.forceConfirmRobot(id, confirmed);
            return ResponseHandler.response(robot);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Forcne odstrani robota vcetne potvrzeneho stavu
     * POZOR: Toto muze narusit integritu dat souteze!
     * Pouzivat pouze ve vyjimecnych situacich.
     * 
     * @param id ID robota
     * @return Informace o stavu provedeneho requestu
     */
    @DeleteMapping("/robot/forceRemove")
    Response forceRemoveRobot(@RequestParam Long id) {
        try {
            this.adminService.forceRemoveRobot(id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }
}
