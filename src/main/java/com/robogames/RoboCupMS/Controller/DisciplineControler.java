package com.robogames.RoboCupMS.Controller;

import java.util.List;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Response;
import com.robogames.RoboCupMS.ResponseHandler;
import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Business.Enum.EScoreAggregation;
import com.robogames.RoboCupMS.Business.Service.DisciplineService;
import com.robogames.RoboCupMS.Entity.Discipline;

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
@RequestMapping(GlobalConfig.API_PREFIX + "/discipline")
public class DisciplineControler {

    @Autowired
    private DisciplineService disciplineService;

    /**
     * Navrati vsechny vytvorene discipliny
     * 
     * @return Seznam disciplin
     */
    @GetMapping("/all")
    Response getAll() {
        List<Discipline> all = this.disciplineService.getAll();
        return ResponseHandler.response(all);
    }

    /**
     * Navarti disciplinu s konkretim ID
     * 
     * @param id ID pozadovane discipliny
     * @return Disciplina
     */
    @GetMapping("/get")
    Response get(@RequestParam Long id) {
        Discipline discipline;
        try {
            discipline = this.disciplineService.get(id);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(discipline);
    }

    /**
     * Vytvori novou disciplinu
     * 
     * @param discipline Nova disciplina
     * @return Informace o stavu provedeneho requestu
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER })
    @PostMapping("/create")
    Response create(@RequestParam String name, @RequestParam String description,
            @RequestParam EScoreAggregation scoreAggregation) {
        try {
            this.disciplineService
                    .create(new Discipline(name, description, scoreAggregation));
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Z databaze odstrani disciplinu
     * 
     * @param id ID discipliny, ktera ma byt odstraneni
     * @return Informace o stavu provedeneho requestu
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER })
    @DeleteMapping("/remove")
    Response remove(@RequestParam Long id) {
        try {
            this.disciplineService.remove(id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Upravi disciplinu (nazev nebo popis)
     * 
     * @param id         ID discipliny jejiz data maji byt zmeneny
     * @param discipline Nove data discipliny
     * @return Informace o stavu provedeneho requestu
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER })
    @PutMapping("/edit")
    Response edit(@RequestParam String name, @RequestParam String description,
            @RequestParam EScoreAggregation scoreAggregation, @RequestParam Long id) {
        try {
            this.disciplineService.edit(new Discipline(name, description, scoreAggregation),
                    id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

}
