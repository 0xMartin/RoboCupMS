package com.robogames.RoboCupMS.Controller;

import java.util.List;
import java.util.Map;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Response;
import com.robogames.RoboCupMS.ResponseHandler;
import com.robogames.RoboCupMS.Business.Enum.ECategory;
import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Business.Object.TournamentGenerateRequestDTO;
import com.robogames.RoboCupMS.Business.Object.TournamentPreviewDTO;
import com.robogames.RoboCupMS.Business.Object.TournamentSaveRequestDTO;
import com.robogames.RoboCupMS.Business.Service.TournamentGeneratorService;

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

/**
 * Controller for tournament generation and management
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(GlobalConfig.API_PREFIX + "/tournament")
public class TournamentController {

    @Autowired
    private TournamentGeneratorService tournamentService;

    /**
     * Generate tournament preview (groups + bracket structure)
     * Does not save anything, just returns the structure for visualization
     * 
     * @param request Generation parameters
     * @return Tournament preview
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER })
    @PostMapping("/preview")
    Response generatePreview(@RequestBody TournamentGenerateRequestDTO request) {
        try {
            TournamentPreviewDTO preview = tournamentService.generatePreview(request);
            return ResponseHandler.response(preview);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Save tournament structure (create actual matches)
     * Called after admin reviews and possibly edits the preview
     * 
     * @param request Tournament structure to save
     * @return Map of temporary IDs to actual match IDs
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER })
    @PostMapping("/save")
    Response saveTournament(@RequestBody TournamentSaveRequestDTO request) {
        try {
            Map<String, Long> result = tournamentService.saveTournamentStructure(request);
            return ResponseHandler.response(result);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Start final bracket - move winners from groups to bracket
     * 
     * @param disciplineId Discipline ID
     * @param category Category (LOW_AGE_CATEGORY or HIGH_AGE_CATEGORY)
     * @param year Competition year
     * @param advancingPerGroup Number of robots advancing from each group
     * @return Success message
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER })
    @PostMapping("/startFinal")
    Response startFinal(
            @RequestParam Long disciplineId,
            @RequestParam ECategory category,
            @RequestParam Integer year,
            @RequestParam(defaultValue = "2") Integer advancingPerGroup) {
        try {
            tournamentService.startFinalBracket(disciplineId, category, year, advancingPerGroup);
            return ResponseHandler.response("Final bracket started successfully");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Get standings for a specific group
     * 
     * @param groupId Group identifier
     * @param year Competition year
     * @return List of standings
     */
    @GetMapping("/groupStandings")
    Response getGroupStandings(
            @RequestParam String groupId,
            @RequestParam Integer year) {
        try {
            List<Map<String, Object>> standings = tournamentService.getGroupStandings(groupId, year);
            return ResponseHandler.response(standings);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Check if tournament structure exists for given discipline/category/year
     * 
     * @param disciplineId Discipline ID
     * @param category Category
     * @param year Competition year
     * @return Boolean indicating if tournament exists
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER })
    @GetMapping("/exists")
    Response tournamentExists(
            @RequestParam Long disciplineId,
            @RequestParam ECategory category,
            @RequestParam Integer year) {
        try {
            boolean exists = tournamentService.tournamentExists(disciplineId, category, year);
            return ResponseHandler.response(exists);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Delete tournament structure
     * 
     * @param disciplineId Discipline ID
     * @param category Category
     * @param year Competition year
     * @return Success message
     */
    @Secured({ ERole.Names.ADMIN })
    @DeleteMapping("/delete")
    Response deleteTournament(
            @RequestParam Long disciplineId,
            @RequestParam ECategory category,
            @RequestParam Integer year) {
        try {
            tournamentService.deleteTournamentStructure(disciplineId, category, year);
            return ResponseHandler.response("Tournament structure deleted successfully");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Get tournament status overview - groups with standings, match progress
     * 
     * @param disciplineId Discipline ID
     * @param category Category
     * @param year Competition year
     * @return Tournament status with groups, standings, and match progress
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER })
    @GetMapping("/status")
    Response getTournamentStatus(
            @RequestParam Long disciplineId,
            @RequestParam ECategory category,
            @RequestParam Integer year,
            @RequestParam(defaultValue = "2") Integer advancingPerGroup) {
        try {
            Map<String, Object> status = tournamentService.getTournamentStatus(disciplineId, category, year, advancingPerGroup);
            return ResponseHandler.response(status);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }
}
