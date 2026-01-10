package com.robogames.RoboCupMS.Controller;

import java.util.List;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Response;
import com.robogames.RoboCupMS.ResponseHandler;
import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Business.Object.RobotObj;
import com.robogames.RoboCupMS.Business.Object.RobotProfile;
import com.robogames.RoboCupMS.Business.Object.RobotMatchInfo;
import com.robogames.RoboCupMS.Business.Service.RobotService;
import com.robogames.RoboCupMS.Entity.Robot;

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
@RequestMapping(GlobalConfig.API_PREFIX + "/robot")
public class RobotControler {

    @Autowired
    private RobotService robotService;

    /**
     * Navrati robota s konkretnim ID
     * 
     * @param id ID robota
     * @return Robot
     */
    @GetMapping("/get")
    Response get(@RequestParam Long id) {
        Robot robot;
        try {
            robot = this.robotService.get(id);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(robot);
    }
    
    /**
     * Navrati podrobny profil robota podle id
     * 
     * @param id ID robota
     * @return Profil robota (podrobnejsni info)
     */
    @GetMapping("/profile")
    Response getRobotProfile(@RequestParam Long id) {
        RobotProfile profile;
        try {
            profile = this.robotService.getRobotProfile(id);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(profile);
    }

    /**
     * Navrati vsechny zapasy robota (odehrane i naplanovane)
     * 
     * @param id ID robota
     * @return Seznam zapasu robota
     */
    @GetMapping("/matches")
    Response getRobotMatches(@RequestParam Long id) {
        List<RobotMatchInfo> matches;
        try {
            matches = this.robotService.getRobotMatches(id);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(matches);
    }

    /**
     * Navrati vsechny vytvorene robot pro urcitou registraci tymu
     * 
     * @param year Rocnik souteze
     * @return Seznam vsech robotu
     */
    @GetMapping("/all")
    Response getAll(@RequestParam int year) {
        List<Robot> robots;
        try {
            robots = this.robotService.getAll(year);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(robots);
    }

    /**
     * Navrati vsehcny roboty s potvrzenou registraci
     * 
     * @param year Rocnik souteze
     * @return Seznam robotu s potvrzenou registraci
     */
    @GetMapping("/allConfirmed")
    Response getAllConfirmed(@RequestParam int year) {
        List<Robot> robots;
        try {
            robots = this.robotService.getAllConfirmed(year);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(robots);
    }

    /**
     * Navrati vsehcny roboty pro dany rocnik
     * 
     * @param year Rocnik souteze
     * @return Seznam robotu s potvrzenou registraci
     */
    @GetMapping("/allForYear")
    Response allForYear(@RequestParam int year) {
        List<Robot> robots;
        try {
            robots = this.robotService.allForYear(year);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(robots);
    }

    /**
     * Vytvori noveho robata. Robot je vytvaren na registraci tymu v urcitem
     * rocniku souteze.
     * 
     * @param year     Rocnik souteze
     * @param robotObj Parametry noveho robota
     * @return Informace o stavu provedeneho requestu
     */
    @PostMapping("/create")
    Response create(@RequestParam int year, @RequestBody RobotObj robotObj) {
        try {
            this.robotService.create(year, robotObj);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Odstrani robota
     * 
     * @param year Rocnik souteze
     * @param id   ID robota
     * @return Informace o stavu provedeneho requestu
     */
    @DeleteMapping("/remove")
    Response remove(@RequestParam int year, @RequestParam Long id) {
        try {
            this.robotService.remove(year, id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Zmeni jmeno robota
     * 
     * @param year Rocnik souteze
     * @param id   ID robota
     * @param name Nove jmeno robota
     * @return Informace o stavu provedeneho requestu
     */
    @PutMapping("/rename")
    Response rename(@RequestParam int year, @RequestParam Long id, @RequestParam String name) {
        try {
            this.robotService.rename(year, id, name);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Registuje existujiciho robota do vybrane discipliny
     * 
     * @param robotID      ID robota, ktereho registrujeme
     * @param disciplineID ID discipliny, do ktere chceme robota registrovat
     * @return Informace o stavu provedeneho requestu
     */
    @PutMapping("/register")
    Response register(@RequestParam Long robotID, @RequestParam Long disciplineID) {
        try {
            this.robotService.register(robotID, disciplineID);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Zrusi registraci existujiciho robota
     * 
     * @param id ID robota
     * @return Informace o stavu provedeneho requestu
     */
    @PutMapping("/unregister")
    Response unregister(@RequestParam Long id) {
        try {
            this.robotService.unregister(id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Povrdi nebo nepovrdi registraci robota
     * 
     * @param id        ID robota
     * @param confirmed Registrace je nebo neni povrzena
     * @return Informace o stavu provedeneho requestu
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.ASSISTANT })
    @PutMapping("/confirmRegistration")
    Response confirmRegistration(@RequestParam Long id, @RequestParam Boolean confirmed) {
        try {
            this.robotService.confirmRegistration(id, confirmed);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

}
