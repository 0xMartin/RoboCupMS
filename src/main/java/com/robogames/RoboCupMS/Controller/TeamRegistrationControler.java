package com.robogames.RoboCupMS.Controller;

import java.util.List;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Response;
import com.robogames.RoboCupMS.ResponseHandler;
import com.robogames.RoboCupMS.Business.Enum.ECategory;
import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Business.Object.TeamRegistrationObj;
import com.robogames.RoboCupMS.Business.Service.TeamRegistrationService;
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
@RequestMapping(GlobalConfig.API_PREFIX + "/teamRegistration")
public class TeamRegistrationControler {

    @Autowired
    private TeamRegistrationService registrationService;

    /**
     * Navrati vsechny registrace tymu, ve kterem se uzivatel nachazi (vsehny
     * rocniky, kterych se ucastnil)
     * 
     * @return Seznam vsech registraci
     */
    @GetMapping("/all")
    Response getAll() {
        List<TeamRegistration> all = null;
        try {
            all = this.registrationService.getAll();
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(all);
    }

    /**
     * Registruje tym do souteze (registrovat muze pouze vedouci tymu!!!!!)
     * 
     * @param teamRegistrationObj Parametry nove registrace tymu
     * @return Informace o stavu provedeneho requestu
     */
    @PostMapping("/register")
    Response register(@RequestBody TeamRegistrationObj teamRegistrationObj) {
        try {
            this.registrationService.register(teamRegistrationObj);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Zrusi registraci tymu
     * 
     * @param year Rocni souteze
     * @return Informace o stavu provedeneho requestu
     */
    @DeleteMapping("/unregister")
    Response unregister(@RequestParam int year) {
        try {
            this.registrationService.unregister(year);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Zmeni kategorii tymu. Jiz neni nijak omezovano vekem a tak je mozne zvolit
     * libovolnou.
     * 
     * @param id       ID tymu
     * @param year     Rocnik souteze
     * @param category Nova kategorie, ve ktere bude tym soutezit
     * @return Informace o stavu provedeneho requestu
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.ASSISTANT })
    @PutMapping("/changeCategory")
    public Response changeCategory(@RequestParam long id, @RequestParam int year, @RequestParam ECategory category) {
        try {
            this.registrationService.changeCategory(id, year, category);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Slouci dve ruzne kategorie dohromady. Vybere se jedna kategorie a vsichni, kteri jsou v
     * ni registrovani se pridaji k jine zvolene kategorii.
     * 
     * @param year        Rocnik souteze
     * @param category    Kategorie tymu, ktere se budou presouvat do jine
     * @param newCategory Kategorie, do ktere se presunou vsechny registrovane tymy
     *                    z jejich aktualni kategorie
     * @return Informace o stavu provedeneho requestu
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER })
    @PutMapping("/joinCategory")
    Response joinCategory(@RequestParam int year, @RequestParam ECategory category,
            @RequestParam ECategory newCategory) {
        try {
            this.registrationService.joinCategory(year, category, newCategory);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

}
