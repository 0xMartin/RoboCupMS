package com.robogames.RoboCupMS.Module.OrderManagement;

import java.util.List;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Response;
import com.robogames.RoboCupMS.ResponseHandler;
import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Entity.RobotMatch;
import com.robogames.RoboCupMS.Module.OrderManagement.Bussiness.Object.MultiMatchGroupObj;
import com.robogames.RoboCupMS.Module.OrderManagement.Bussiness.Service.OrderManagementService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Zajistuje zobrazovani aktualniho poradi zapasu a jejich generovani
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(GlobalConfig.MODULE_PREFIX + "/orderManagement")
public class OrderManagement {

    @Autowired
    private OrderManagementService competitionEvaluationService;

    /**
     * Spusti modul pro rizeni poradi. Modul bude mozne spustit jen pro soutez,
     * ktery byla jiz zahajna.
     * 
     * @param year Rocnik souteze
     * @throws Exception
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.ASSISTANT })
    @PutMapping("/run")
    Response run(@RequestParam int year) {
        try {
            this.competitionEvaluationService.run(year);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Navrati informaci o tom zda je servis spusten
     * 
     * @return Stav
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.ASSISTANT, ERole.Names.REFEREE })
    @GetMapping("/isRunning")
    Response isRunning() {
        return ResponseHandler.response(this.competitionEvaluationService.isRunning());
    }

    /**
     * Vyzada refresh systemu, pokud dojde k zamrznuti
     * 
     * @return Informace o stavu provedeneho requestu
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.ASSISTANT })
    @PutMapping("/requestRefresh")
    Response requestRefresh() {
        try {
            this.competitionEvaluationService.requestRefresh();
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Navrati seznam vsech zapasu, ktere maji byt nyni odehrany na prislusnych
     * hristich
     * 
     * @param year     Rocnik souteze
     * @param category Soutezni kategorie
     * @return Vsechny zapasy, ktere maji byt nyni odehrany na prislusnych hristich
     */
    @GetMapping("/currentMatches")
    Response currentMatches() {
        List<RobotMatch> matches;
        try {
            matches = this.competitionEvaluationService.currentMatches();
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(matches);
    }

    /**
     * Vyzada zmenu poradi zapasu ve fronte
     * 
     * @param id ID zapasu, o kterem rozhodci rozhodne, aby byl odehran v danou
     *           chvili. Zapas s timto ID bude presunut na prvni misto ve fronte.
     *           (pokud bude zadana zaporna neplatna hodnota pak system vybere
     *           náhodne ze seznamu cekajicich zapasu)
     * @return Informace o stavu provedeneho requestu
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.REFEREE })
    @PutMapping("/requestAnotherMatch")
    Response requestAnotherMatch(@RequestParam long id) {
        try {
            this.competitionEvaluationService.requestAnotherMatch(id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Navrati pro robota seznam vsech nadchazejicich zapasu
     * 
     * @param id ID robota
     * @return Seznam vsech zapasu robota, ktere jeste cekaji na odehrani
     */
    @GetMapping("/upcommingMatches")
    Response upcommingMatches(@RequestParam long id) {
        List<RobotMatch> matches;
        try {
            matches = this.competitionEvaluationService.upcommingMatches(id);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(matches);
    }

    /**
     * Vygeneruje skupinove zapasy "kazdy s kazdym" (sumo, robo strong, ...)
     * 
     * @param multiMatchGroupObj Objekt definujici parametry pro vygenerovani vsech
     *                           zapasu
     * @return Navrati identifikacni cislo tvurce zapasovych skupin (nasledne muze
     *         byt uplatneno pro odstraneni zapasu)
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.REFEREE })
    @PostMapping("/generateMatches")
    Response generateMatches(@RequestBody MultiMatchGroupObj multiMatchGroupObj) {
        long creatorIdentifier;
        try {
            creatorIdentifier = this.competitionEvaluationService.generateMatches(multiMatchGroupObj);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(creatorIdentifier);
    }

}
