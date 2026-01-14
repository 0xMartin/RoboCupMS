package com.robogames.RoboCupMS.Module.OrderManagement.Bussiness.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.robogames.RoboCupMS.Communication;
import com.robogames.RoboCupMS.Business.Enum.ECategory;
import com.robogames.RoboCupMS.Business.Enum.EMatchState;
import com.robogames.RoboCupMS.Business.Service.MatchService;
import com.robogames.RoboCupMS.Communication.CallBack;
import com.robogames.RoboCupMS.Entity.Competition;
import com.robogames.RoboCupMS.Entity.Robot;
import com.robogames.RoboCupMS.Entity.RobotMatch;
import com.robogames.RoboCupMS.Module.OrderManagement.Bussiness.Object.MatchQueue;
import com.robogames.RoboCupMS.Module.OrderManagement.Bussiness.Object.ScheduledMatchInfo;
import com.robogames.RoboCupMS.Repository.CompetitionRepository;
import com.robogames.RoboCupMS.Repository.RobotMatchRepository;
import com.robogames.RoboCupMS.Repository.RobotRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Service for managing match order and scheduling
 */
@Service
public class OrderManagementService {

    private static final Logger logger = LoggerFactory.getLogger(OrderManagementService.class);

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private RobotMatchRepository robotMatchRepository;

    @Autowired
    private RobotRepository robotRepository;

    /**
     * Match queues for each playground
     */
    private final static Map<Long, MatchQueue> MATCH_QUEUES = Collections
            .synchronizedMap(new HashMap<Long, MatchQueue>());

    /**
     * Competition year (volatile for thread-safety)
     */
    private static volatile int YEAR = -1;

    public OrderManagementService() {
        // Listen to the application's communication system
        Communication.getInstance().getCallBacks().add(new CallBack() {
            @Override
            public void callBack(Object sender, Object data) {
                // Refresh only when changes are made to matches
                if (sender instanceof MatchService) {
                    if (data instanceof MatchService.Message) {
                        MatchService.Message msg = (MatchService.Message) data;
                        if (msg.equals(MatchService.Message.CREATE)
                                || msg.equals(MatchService.Message.WRITE_SCORE)
                                || msg.equals(MatchService.Message.REMATCH)
                                || msg.equals(MatchService.Message.REMOVE)
                                || msg.equals(MatchService.Message.UPDATE)) {
                            // Refresh the order management system
                            refreshSystem();
                        }
                    }
                }
            }
        });
    }

    /**
     * Automatically start Order Management Service after application starts,
     * if there is an active competition.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("Application started, checking for active competitions...");
        
        // Find all started competitions
        List<Competition> startedCompetitions = this.competitionRepository.findAll()
                .stream()
                .filter(Competition::getStarted)
                .collect(Collectors.toList());
        
        if (!startedCompetitions.isEmpty()) {
            // Take the latest (most recent) started competition
            Competition latestStarted = startedCompetitions.stream()
                    .max((c1, c2) -> Integer.compare(c1.getYear(), c2.getYear()))
                    .orElse(null);
            
            if (latestStarted != null) {
                try {
                    logger.info("Found active competition for year {}, starting Order Management Service...", 
                            latestStarted.getYear());
                    this.run(latestStarted.getYear());
                    logger.info("Order Management Service started successfully for year {}", 
                            latestStarted.getYear());
                } catch (Exception e) {
                    logger.error("Failed to start Order Management Service: {}", e.getMessage());
                }
            }
        } else {
            logger.info("No active competitions found, Order Management Service not started");
        }
    }

    /**
     * Start the order management module. Can only be started for a competition
     * that has already been started.
     * 
     * @param year Competition year
     * @throws Exception
     */
    public void run(int year) throws Exception {
        Optional<Competition> competition = this.competitionRepository.findByYear(year);
        if (!competition.isPresent()) {
            throw new Exception(String.format("failure, competition [%d] not exists", year));
        }

        if (!competition.get().getStarted()) {
            throw new Exception(String.format("failure, competition [%d] has not started yet", year));
        }

        // Set competition year
        OrderManagementService.YEAR = year;

        // Refresh the system
        this.refreshSystem();
    }

    /**
     * Returns whether the service is running
     * 
     * @return Status
     */
    public boolean isRunning() {
        return OrderManagementService.YEAR != -1;
    }

    /**
     * Request a system refresh if frozen
     * 
     * @throws Exception
     */
    public void requestRefresh() throws Exception {
        if (OrderManagementService.YEAR == -1) {
            throw new Exception("Order Management Service is not running!");
        }

        OrderManagementService.MATCH_QUEUES.clear();

        this.refreshSystem();
    }

    /**
     * Refresh the system if it is running (does nothing if not running).
     * Use this when data changes (e.g., robot name edit) that should be reflected
     * in the queued matches.
     */
    public void refreshIfRunning() {
        if (OrderManagementService.YEAR != -1) {
            this.refreshSystem();
        }
    }

    /**
     * Return list of all matches that should be played now on their respective playgrounds
     * 
     * @return All matches that should be played now
     */
    public List<RobotMatch> currentMatches() throws Exception {
        if (OrderManagementService.YEAR == -1) {
            throw new Exception("failure, order Management Service is not running!");
        }

        List<RobotMatch> matches = new ArrayList<RobotMatch>();

        OrderManagementService.MATCH_QUEUES.forEach((p, queue) -> {
            RobotMatch first = queue.getFirst();
            if (first != null) {
                matches.add(first);
            }
        });

        return matches;
    }

    /**
     * Skip the current match on a playground - moves first match to the end of queue
     * 
     * @param playgroundId Playground ID
     */
    public void skipCurrentMatch(long playgroundId) throws Exception {
        if (OrderManagementService.YEAR == -1) {
            throw new Exception("failure, order Management Service is not running!");
        }

        MatchQueue matchQueue = OrderManagementService.MATCH_QUEUES.get(playgroundId);

        if (matchQueue == null) {
            throw new Exception(String.format("failure, no match queue exists for playground ID [%d]", playgroundId));
        }

        // Move first match to the end of the queue
        if (!matchQueue.moveFirstToEnd()) {
            throw new Exception("failure, queue is empty or has only one match");
        }
        
        logger.info(String.format("[Playground ID: %d] skipped current match, moved to end of queue", playgroundId));
    }

    /**
     * Get current match for a specific playground (first in queue)
     * 
     * @param playgroundId Playground ID
     * @return Current match or null if no match in queue
     */
    public RobotMatch getCurrentMatch(long playgroundId) throws Exception {
        if (OrderManagementService.YEAR == -1) {
            throw new Exception("failure, order Management Service is not running!");
        }

        MatchQueue matchQueue = OrderManagementService.MATCH_QUEUES.get(playgroundId);

        if (matchQueue == null) {
            return null;
        }

        return matchQueue.getFirst();
    }

    /**
     * Get list of all upcoming matches for a robot
     * 
     * @param id Robot ID
     * @return List of all waiting matches for the robot
     */
    public List<RobotMatch> upcomingMatches(long id) throws Exception {
        // Verify robot exists
        Optional<Robot> robot = this.robotRepository.findById(id);
        if (!robot.isPresent()) {
            throw new Exception(String.format("failure, robot with ID [%d] not exists", id));
        }

        // List of all robot matches waiting to be played
        return robot.get().getMatches().stream()
                .filter((m) -> (m.getState().getName() == EMatchState.WAITING
                        || m.getState().getName() == EMatchState.REMATCH))
                .collect(Collectors.toList());
    }

    /**
     * Get all scheduled matches (WAITING or REMATCH) for public display.
     * Can be filtered by disciplines and categories.
     * 
     * @param disciplineIds List of discipline IDs to filter (null = all)
     * @param categories    List of categories to filter (null = all)
     * @return List of matches with detailed information
     */
    public List<ScheduledMatchInfo> getAllScheduledMatches(List<Long> disciplineIds, List<ECategory> categories) {
        List<ScheduledMatchInfo> result = new ArrayList<>();
        
        // Get all matches from all queues
        OrderManagementService.MATCH_QUEUES.forEach((playgroundId, queue) -> {
            queue.getMatches().stream()
                .filter(m -> m.getState().getName() == EMatchState.WAITING 
                          || m.getState().getName() == EMatchState.REMATCH)
                .filter(m -> m.hasRobots()) // Only include matches with robots assigned
                .filter(m -> disciplineIds == null || disciplineIds.isEmpty() 
                          || disciplineIds.contains(m.getDisciplineID()))
                .filter(m -> categories == null || categories.isEmpty() 
                          || (m.getRobotA() != null && categories.contains(m.getRobotA().getCategory())))
                .forEach(m -> result.add(new ScheduledMatchInfo(m)));
        });
        
        // If service is not running, try to load directly from DB
        if (OrderManagementService.YEAR == -1) {
            this.robotMatchRepository.findAll().stream()
                .filter(m -> m.getState().getName() == EMatchState.WAITING 
                          || m.getState().getName() == EMatchState.REMATCH)
                .filter(m -> m.hasRobots())
                .filter(m -> disciplineIds == null || disciplineIds.isEmpty() 
                          || disciplineIds.contains(m.getDisciplineID()))
                .filter(m -> categories == null || categories.isEmpty() 
                          || (m.getRobotA() != null && categories.contains(m.getRobotA().getCategory())))
                .forEach(m -> result.add(new ScheduledMatchInfo(m)));
        }
        
        return result;
    }

    /**
     * Refresh the order management system
     * (called automatically on each match state change)
     */
    private synchronized void refreshSystem() {
        if (OrderManagementService.YEAR == -1) {
            logger.error("Order Management Service is not running!");
            return;
        }

        logger.info("OrderManagementService refresh");

        // Synchronize
        OrderManagementService.MATCH_QUEUES.forEach((p, queue) -> {
            // Synchronize data
            queue.synchronize(this.robotMatchRepository);
            // Remove completed matches
            int cnt = queue.removeAllDone();
            logger.info(String.format("[Playground ID: %d] removed from queue: %d", p, cnt));
        });

        // Add all matches waiting to be played (only matches WITH robots - can't call robots if there are none)
        List<RobotMatch> matches = this.robotMatchRepository.findAll().stream()
                .filter(m -> m.hasRobots()) // Only matches with at least one robot
                .filter(m -> {
                    // Filter by year - check if any robot is from this year
                    if (m.getRobotA() != null) {
                        return m.getRobotA().getTeamRegistration().getCompetitionYear() == YEAR;
                    }
                    if (m.getRobotB() != null) {
                        return m.getRobotB().getTeamRegistration().getCompetitionYear() == YEAR;
                    }
                    return false; // Should never reach here since hasRobots() is true
                })
                .collect(Collectors.toList());

        for (RobotMatch m : matches) {
            MatchQueue queue = OrderManagementService.MATCH_QUEUES.get(m.getPlayground().getID());
            if (queue == null) {
                queue = new MatchQueue(m.getPlayground());
                OrderManagementService.MATCH_QUEUES.put(m.getPlayground().getID(), queue);
            }
            if (queue.add(m)) {
                logger.info(String.format("[Playground ID: %d] added new match with ID [%d]", 
                        m.getPlayground().getID(), m.getID()));
            }
        }
    }

}
