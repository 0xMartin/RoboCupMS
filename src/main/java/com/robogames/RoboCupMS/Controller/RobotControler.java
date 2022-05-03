package com.robogames.RoboCupMS.Controller;

import java.util.List;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Response;
import com.robogames.RoboCupMS.ResponseHandler;
import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Business.Service.RobotService;
import com.robogames.RoboCupMS.Entity.Robot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
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
     * Vytvori noveho robata. Robot je vytvaren na registraci tymu v urcitem
     * konkretim rocniku souteze.
     * 
     * @param year Rocnik souteze
     * @return Informace o stavu provedene operace
     */
    @PostMapping("/create")
    Response create(@RequestParam int year, @RequestParam String name) {
        try {
            this.robotService.create(year, name);
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
     * @return Informace o stavu provedene operace
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
     * @return Informace o stavu provedene operace
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
     * @return Informace o stavu provedene operace
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
     * @return Informace o stavu provedene operace
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
     * @return Informace o stavu provedene operace
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