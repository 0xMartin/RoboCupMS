package com.robogames.RoboCupMS.Module.OrderManagement;

import java.util.List;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Response;
import com.robogames.RoboCupMS.ResponseHandler;
import com.robogames.RoboCupMS.Business.Enum.ECategory;
import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Entity.RobotMatch;
import com.robogames.RoboCupMS.Module.OrderManagement.Bussiness.Object.ScheduledMatchInfo;
import com.robogames.RoboCupMS.Module.OrderManagement.Bussiness.Service.OrderManagementService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing match order and scheduling display
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(GlobalConfig.MODULE_PREFIX + "/orderManagement")
public class OrderManagement {

    @Autowired
    private OrderManagementService orderManagementService;

    /**
     * Start the order management module. Can only be started for a competition
     * that has already been started.
     * 
     * @param year Competition year
     * @throws Exception
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.ASSISTANT })
    @PutMapping("/run")
    Response run(@RequestParam int year) {
        try {
            this.orderManagementService.run(year);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Returns whether the service is running
     * 
     * @return Status
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.ASSISTANT, ERole.Names.REFEREE })
    @GetMapping("/isRunning")
    Response isRunning() {
        return ResponseHandler.response(this.orderManagementService.isRunning());
    }

    /**
     * Request a system refresh if frozen
     * 
     * @return Status info
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.ASSISTANT })
    @PutMapping("/requestRefresh")
    Response requestRefresh() {
        try {
            this.orderManagementService.requestRefresh();
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Return list of all matches that should be played now on their respective playgrounds
     * 
     * @return All matches that should be played now
     */
    @GetMapping("/currentMatches")
    Response currentMatches() {
        List<RobotMatch> matches;
        try {
            matches = this.orderManagementService.currentMatches();
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(matches);
    }

    /**
     * Request a change in match order in the queue
     * 
     * @param id Match ID to be moved to the front of the queue
     * @return Status info
     */
    @Secured({ ERole.Names.ADMIN, ERole.Names.LEADER, ERole.Names.REFEREE })
    @PutMapping("/requestAnotherMatch")
    Response requestAnotherMatch(@RequestParam long id) {
        try {
            this.orderManagementService.requestAnotherMatch(id);
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Get list of all upcoming matches for a robot
     * 
     * @param id Robot ID
     * @return List of all waiting matches for the robot
     */
    @GetMapping("/upcomingMatches")
    Response upcomingMatches(@RequestParam long id) {
        List<RobotMatch> matches;
        try {
            matches = this.orderManagementService.upcomingMatches(id);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        return ResponseHandler.response(matches);
    }

    /**
     * Get all scheduled matches (WAITING, REMATCH) for public display.
     * This endpoint is public - no authentication required.
     * 
     * @param disciplineIds Optional list of discipline IDs to filter
     * @param categories Optional list of categories to filter (LOW_AGE_CATEGORY, HIGH_AGE_CATEGORY)
     * @return List of scheduled matches with robot and team info
     */
    @GetMapping("/scheduledMatches")
    Response getScheduledMatches(
            @RequestParam(required = false) List<Long> disciplineIds,
            @RequestParam(required = false) List<String> categories) {
        try {
            List<ECategory> categoryEnums = null;
            if (categories != null && !categories.isEmpty()) {
                categoryEnums = categories.stream()
                        .map(c -> ECategory.valueOf(c.toUpperCase()))
                        .toList();
            }
            List<ScheduledMatchInfo> matches = this.orderManagementService.getAllScheduledMatches(disciplineIds, categoryEnums);
            return ResponseHandler.response(matches);
        } catch (IllegalArgumentException ex) {
            return ResponseHandler.error("Invalid category value: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

}
