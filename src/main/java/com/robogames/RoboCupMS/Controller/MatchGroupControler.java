package com.robogames.RoboCupMS.Controller;

import java.util.List;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Response;
import com.robogames.RoboCupMS.ResponseHandler;
import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Business.Object.MatchGroupObj;
import com.robogames.RoboCupMS.Business.Service.MatchGroupService;
import com.robogames.RoboCupMS.Entity.MatchGroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(GlobalConfig.API_PREFIX + "/group")
public class MatchGroupControler {

    @Autowired
    private MatchGroupService groupService;

    /**
     * Navrati vsechny zapasove skupiny
     * 
     * @return Seznam vsech skupin
     */
    @GetMapping("/all")
    Response getAll() {
        List<MatchGroup> all = this.groupService.getAll();
        return ResponseHandler.response(all);
    }

    /**
     * Navrti vsechny zapasove skupiny pro specifikovany identifikator tvurce
     * skupiny
     * 
     * @param creatorID Identifikator tvurce skupiny
     * @return Seznam vsech skupin
     */
    @GetMapping("/getByCID")
    Response getByCID(@RequestParam Long creatorID) {
        List<MatchGroup> groups;
        try {
            groups = this.groupService.getByCID(creatorID);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(groups);
    }

    /**
     * Navrati skupinu s konkretnim ID
     * 
     * @param id ID skupiny
     * @return Zapasova skupina
     */
    @GetMapping("/getByID")
    Response getByID(@RequestParam Long id) {
        MatchGroup group;
        try {
            group = this.groupService.getByID(id);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(group);
    }

    /**
     * Vytvori novou zapasovou skupinu
     * 
     * @param groupObj Parametry nove zapasove skupiny
     * @return Informace o stavu provedeneho requestu
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.REFEREE })
    @PostMapping("/create")
    Response create(@RequestBody MatchGroupObj groupObj) {
        try {
            this.groupService.create(groupObj);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Odstrani skupinu
     * 
     * @param id ID skupiny
     * @return Informace o stavu provedeneho requestu
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.REFEREE })
    @DeleteMapping("/remove")
    Response remove(@RequestParam Long id) {
        try {
            this.groupService.remove(id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Odstrani vsechny skupiny s odpovidajicim ID tvurce skupiny
     * 
     * @param creatorid Identifikator tvurce skupiny
     * @return Informace o stavu provedeneho requestu
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.REFEREE })
    @DeleteMapping("/removeAll")
    Response removeAll(@RequestParam Long creatorid) {
        try {
            this.groupService.removeAll(creatorid);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

}
