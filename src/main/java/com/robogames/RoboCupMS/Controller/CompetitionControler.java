package com.robogames.RoboCupMS.Controller;

import java.util.List;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Response;
import com.robogames.RoboCupMS.ResponseHandler;
import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Business.Object.CompetitionObj;
import com.robogames.RoboCupMS.Business.Service.CompetitionService;
import com.robogames.RoboCupMS.Entity.Competition;
import com.robogames.RoboCupMS.Entity.TeamRegistration;

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
@RequestMapping(GlobalConfig.API_PREFIX + "/competition")
public class CompetitionControler {

    @Autowired
    private CompetitionService competitionService;

    /**
     * Navrati vsechny uskutecnene a naplanovane rocniky soutezi
     * 
     * @return List soutezi
     */
    @GetMapping("/all")
    Response getAll() {
        List<Competition> all = this.competitionService.getAll();
        return ResponseHandler.response(all);
    }

    /**
     * Navrati vsechny registrace tymu pro dany rocnik souteze
     * 
     * @param year Rocnik souteze
     * @return List vsech registraci
     */
    @GetMapping("/allRegistrations")
    Response allRegistrations(@RequestParam int year) {
        List<TeamRegistration> registrations;
        try {
            registrations = this.competitionService.allRegistrations(year);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(registrations);
    }

    /**
     * Vytvori novy rocnik souteze
     * 
     * @param compatition Nova soutez
     * @return Informace o stavu provedeneho requestu
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER })
    @PostMapping("/create")
    Response create(@RequestBody CompetitionObj compatitionObj) {
        try {
            this.competitionService.create(compatitionObj);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Odstrani soutez z databaze a s ni i vsechny data
     * 
     * @param id ID souteze
     * @return Informace o stavu provedeneho requestu
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER })
    @DeleteMapping("/remove")
    Response remove(@RequestParam Long id) {
        try {
            this.competitionService.remove(id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Upravi parametry souteze, mozne jen pokud jeste nezacala
     * 
     * @param id             ID souteze jejiz parametry maji byt upraveny
     * @param compatitionObj Nove parametry souteze
     * @return Informace o stavu provedeneho requestu
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER })
    @PutMapping("/edit")
    Response edit(@RequestParam Long id, @RequestBody CompetitionObj compatitionObj) {
        try {
            this.competitionService.edit(id, compatitionObj);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Zahaji soutez
     * 
     * @param id ID souteze, ktera ma byt zahajena
     * @return Informace o stavu provedeneho requestu
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER })
    @PutMapping("/start")
    Response start(@RequestParam Long id) {
        try {
            this.competitionService.start(id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

}
