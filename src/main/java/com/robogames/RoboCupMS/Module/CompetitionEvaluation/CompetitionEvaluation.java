package com.robogames.RoboCupMS.Module.CompetitionEvaluation;

import java.util.List;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Response;
import com.robogames.RoboCupMS.ResponseHandler;
import com.robogames.RoboCupMS.Business.Enum.ECategory;
import com.robogames.RoboCupMS.Module.CompetitionEvaluation.Bussiness.Object.OrderObj;
import com.robogames.RoboCupMS.Module.CompetitionEvaluation.Bussiness.Object.RobotScore;
import com.robogames.RoboCupMS.Module.CompetitionEvaluation.Bussiness.Object.TeamScore;
import com.robogames.RoboCupMS.Module.CompetitionEvaluation.Bussiness.Service.CompetitionEvaluationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ulehcuje praci z vyhodnocovanim souteze
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(GlobalConfig.MODULE_PREFIX + "/competitionEvaluation")
public class CompetitionEvaluation {

    @Autowired
    private CompetitionEvaluationService competitionEvaluationService;

    /**
     * Navrati skore vsech robotu, kteri soutezili v danem rocniku
     * 
     * @param year     Rocnik souteze
     * @param category Kategorie, pro kterou má zobrazit výsledky
     * @return Seznam vsech robotu a jejich skore v soutezi
     */
    @GetMapping("/scoreOfAll")
    Response getScoreOfAll(@RequestParam int year, @RequestParam ECategory category) {
        List<RobotScore> scoreOfAll;
        try {
            scoreOfAll = this.competitionEvaluationService.getScoreOfAll(year, category);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(scoreOfAll);
    }

    /**
     * Navrati skore vsech robotu urciteho tymu
     * 
     * @param year Rocnik
     * @param id   ID tymu
     * @return Navrati skore vsech reobotu v tymu
     */
    @GetMapping("/scoreOfTeam")
    Response getScoreOfTeam(@RequestParam int year, @RequestParam long id) {
        TeamScore scoreOfTeam;
        try {
            scoreOfTeam = this.competitionEvaluationService.getScoreOfTeam(year, id);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(scoreOfTeam);
    }

    /**
     * Navrati skore jednoho konkreniho robota
     * 
     * @param year Rocnik souteze
     * @param id   ID robota
     * @return Navrati skore robota
     */
    @GetMapping("/scoreOfRobot")
    Response getScoreOfRobot(@RequestParam int year, @RequestParam long id) {
        RobotScore scoreOfRobot;
        try {
            scoreOfRobot = this.competitionEvaluationService.getScoreOfRobot(year, id);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(scoreOfRobot);
    }

    /**
     * Navrati umisteni robotu v konkretni discipline v ramci soutezni kategorie
     * 
     * @param year     Rocnik souteze
     * @param category Soutezni kategorie
     * @param id       ID discipliny
     * @return Poradi vsech robotu, kteri soutezili v dane discipline + kategorii
     */
    @GetMapping("/getOrder")
    Response getOrder(@RequestParam int year, @RequestParam ECategory category, @RequestParam long id) {
        List<OrderObj> winners;
        try {
            winners = this.competitionEvaluationService.getOrder(year, category, id);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(winners);
    }

}
