package com.robogames.RoboCupMS.Controller;

import java.util.List;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Response;
import com.robogames.RoboCupMS.ResponseHandler;
import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Business.Enum.ETournamentPhase;
import com.robogames.RoboCupMS.Business.Object.MatchScoreObj;
import com.robogames.RoboCupMS.Business.Object.RobotMatchObj;
import com.robogames.RoboCupMS.Business.Service.MatchService;
import com.robogames.RoboCupMS.Entity.RobotMatch;

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

/**
 * Controller for robot match management
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(GlobalConfig.API_PREFIX + "/match")
public class MatchControler {

    @Autowired
    private MatchService matchService;

    /**
     * Get all matches
     * 
     * @return List of all matches
     */
    @GetMapping("/all")
    Response getAll() {
        List<RobotMatch> all = this.matchService.getAll();
        return ResponseHandler.response(all);
    }

    /**
     * Get match by ID
     * 
     * @param id Match ID
     * @return The match
     */
    @GetMapping("/getByID")
    Response getByID(@RequestParam Long id) {
        try {
            RobotMatch match = this.matchService.getByID(id);
            return ResponseHandler.response(match);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Get all matches for a specific competition year
     * 
     * @param year Competition year
     * @return List of matches
     */
    @GetMapping("/allByYear")
    Response allByYear(@RequestParam int year) {
        try {
            List<RobotMatch> matches = this.matchService.allByYear(year);
            return ResponseHandler.response(matches);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Get all distinct group names for matches in a specific competition year
     * 
     * @param year Competition year
     * @return List of group names
     */
    @GetMapping("/groups")
    Response getGroups(@RequestParam int year) {
        try {
            List<String> groups = this.matchService.getGroupsByYear(year);
            return ResponseHandler.response(groups);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Get all matches for a specific group in a competition year
     * 
     * @param year Competition year
     * @param group Group name
     * @return List of matches
     */
    @GetMapping("/byGroup")
    Response getByGroup(@RequestParam int year, @RequestParam String group) {
        try {
            List<RobotMatch> matches = this.matchService.getByGroup(year, group);
            return ResponseHandler.response(matches);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Get all matches for a specific playground
     * 
     * @param playgroundID Playground ID
     * @return List of matches
     */
    @GetMapping("/byPlayground")
    Response getByPlayground(@RequestParam Long playgroundID) {
        try {
            List<RobotMatch> matches = this.matchService.getByPlayground(playgroundID);
            return ResponseHandler.response(matches);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Get all waiting/scheduled matches for a playground
     * 
     * @param playgroundID Playground ID
     * @return List of waiting matches
     */
    @GetMapping("/waitingByPlayground")
    Response getWaitingMatches(@RequestParam Long playgroundID) {
        try {
            List<RobotMatch> matches = this.matchService.getWaitingMatches(playgroundID);
            return ResponseHandler.response(matches);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Get matches by tournament phase
     * 
     * @param phase Tournament phase (PRELIMINARY, SEMIFINAL, FINAL, etc.)
     * @return List of matches
     */
    @GetMapping("/byPhase")
    Response getByPhase(@RequestParam ETournamentPhase phase) {
        try {
            List<RobotMatch> matches = this.matchService.getByPhase(phase);
            return ResponseHandler.response(matches);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Create a new match (schedule it)
     * Can be created without robots (just scheduled) or with one or two robots
     * 
     * @param matchObj Match parameters
     * @return Created match info
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.REFEREE })
    @PostMapping("/create")
    Response create(@RequestBody RobotMatchObj matchObj) {
        try {
            RobotMatch match = this.matchService.create(matchObj);
            return ResponseHandler.response(match);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Update an existing match
     * 
     * @param id Match ID
     * @param matchObj New match parameters
     * @return Success message
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.REFEREE })
    @PutMapping("/update")
    Response update(@RequestParam Long id, @RequestBody RobotMatchObj matchObj) {
        try {
            this.matchService.update(id, matchObj);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Assign robots to an existing match
     * 
     * @param id Match ID
     * @param robotAID Robot A ID (optional)
     * @param robotBID Robot B ID (optional)
     * @return Success message
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.REFEREE })
    @PutMapping("/assignRobots")
    Response assignRobots(@RequestParam Long id, 
                          @RequestParam(required = false) Long robotAID,
                          @RequestParam(required = false) Long robotBID) {
        try {
            this.matchService.assignRobots(id, robotAID, robotBID);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Remove a match
     * 
     * @param id Match ID
     * @return Success message
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.REFEREE })
    @DeleteMapping("/remove")
    Response remove(@RequestParam Long id) {
        try {
            this.matchService.remove(id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Write scores for a match
     * Automatically marks the match as done and handles bracket progression
     * 
     * @param scoreObj Score data (matchID, scoreA, scoreB)
     * @return Success message
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.REFEREE })
    @PutMapping("/writeScore")
    Response writeScore(@RequestBody MatchScoreObj scoreObj) {
        try {
            this.matchService.writeScore(scoreObj);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Request a rematch - reset scores and mark for replay
     * 
     * @param id Match ID
     * @return Success message
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.REFEREE })
    @PutMapping("/rematch")
    Response rematch(@RequestParam Long id) {
        try {
            this.matchService.rematch(id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

}
