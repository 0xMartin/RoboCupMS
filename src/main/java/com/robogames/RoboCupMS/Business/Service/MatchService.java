package com.robogames.RoboCupMS.Business.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.robogames.RoboCupMS.Communication;
import com.robogames.RoboCupMS.Business.Enum.EMatchState;
import com.robogames.RoboCupMS.Business.Enum.ETournamentPhase;
import com.robogames.RoboCupMS.Business.Object.MatchScoreObj;
import com.robogames.RoboCupMS.Business.Object.RobotMatchObj;
import com.robogames.RoboCupMS.Entity.Competition;
import com.robogames.RoboCupMS.Entity.Discipline;
import com.robogames.RoboCupMS.Entity.MatchState;
import com.robogames.RoboCupMS.Entity.Playground;
import com.robogames.RoboCupMS.Entity.Robot;
import com.robogames.RoboCupMS.Entity.RobotMatch;
import com.robogames.RoboCupMS.Entity.ScoreType;
import com.robogames.RoboCupMS.Entity.TournamentPhase;
import com.robogames.RoboCupMS.Repository.MatchStateRepository;
import com.robogames.RoboCupMS.Repository.PlaygroundRepository;
import com.robogames.RoboCupMS.Repository.RobotMatchRepository;
import com.robogames.RoboCupMS.Repository.RobotRepository;
import com.robogames.RoboCupMS.Repository.TournamentPhaseRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for managing robot matches
 */
@Service
public class MatchService {

    @Autowired
    private RobotMatchRepository robotMatchRepository;

    @Autowired
    private MatchStateRepository matchStateRepository;

    @Autowired
    private RobotRepository robotRepository;

    @Autowired
    private PlaygroundRepository playgroundRepository;

    @Autowired
    private TournamentPhaseRepository tournamentPhaseRepository;

    /**
     * Message types for communication system
     */
    public static enum Message {
        CREATE,
        REMOVE,
        WRITE_SCORE,
        REMATCH,
        UPDATE
    }

    /**
     * Get all matches
     * 
     * @return List of all matches
     */
    public List<RobotMatch> getAll() {
        return this.robotMatchRepository.findAll();
    }

    /**
     * Get match by ID
     * 
     * @param id Match ID
     * @return The match
     * @throws Exception if match not found
     */
    public RobotMatch getByID(Long id) throws Exception {
        Optional<RobotMatch> match = this.robotMatchRepository.findById(id);
        if (!match.isPresent()) {
            throw new Exception(String.format("failure, match with ID [%d] not exists", id));
        }
        return match.get();
    }

    /**
     * Get all matches for a specific competition year
     * 
     * @param year Competition year
     * @return List of matches for that year
     */
    public List<RobotMatch> allByYear(int year) {
        return this.robotMatchRepository.findAll().stream()
                .filter(m -> {
                    // Check if match has any robot to determine the year
                    if (m.getRobotA() != null) {
                        return m.getRobotA().getTeamRegistration().getCompetitionYear() == year;
                    }
                    if (m.getRobotB() != null) {
                        return m.getRobotB().getTeamRegistration().getCompetitionYear() == year;
                    }
                    // Fallback: check stored competition year
                    Integer storedYear = m.getCompetitionYear();
                    return storedYear != null && storedYear == year;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all distinct group names for matches in a specific competition year
     * 
     * @param year Competition year
     * @return List of distinct group names (excluding null)
     */
    public List<String> getGroupsByYear(int year) {
        return this.allByYear(year).stream()
                .map(RobotMatch::getGroup)
                .filter(group -> group != null && !group.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get all matches for a specific group in a competition year
     * 
     * @param year Competition year
     * @param group Group name
     * @return List of matches in that group
     */
    public List<RobotMatch> getByGroup(int year, String group) {
        return this.allByYear(year).stream()
                .filter(m -> group.equals(m.getGroup()))
                .collect(Collectors.toList());
    }

    /**
     * Get all matches for a specific playground and year
     * 
     * @param year Competition year
     * @param playgroundID Playground ID
     * @return List of matches on that playground for that year
     */
    public List<RobotMatch> getByPlayground(int year, Long playgroundID) throws Exception {
        Optional<Playground> playground = this.playgroundRepository.findById(playgroundID);
        if (!playground.isPresent()) {
            throw new Exception(String.format("failure, playground with ID [%d] not exists", playgroundID));
        }
        return this.allByYear(year).stream()
                .filter(m -> m.getPlaygroundID().equals(playgroundID))
                .collect(Collectors.toList());
    }

    /**
     * Create a new match (schedule it)
     * Can be created without robots (just scheduled) or with one or two robots
     * 
     * @param matchObj Match parameters
     * @return The created match
     * @throws Exception on validation errors
     */
    public RobotMatch create(RobotMatchObj matchObj) throws Exception {
        // Validate playground exists
        Optional<Playground> playground = this.playgroundRepository.findById(matchObj.getPlaygroundID());
        if (!playground.isPresent()) {
            throw new Exception(
                    String.format("failure, playground with ID [%d] not exists", matchObj.getPlaygroundID()));
        }

        Robot robotA = null;
        Robot robotB = null;
        Discipline discipline = playground.get().getDiscipline();

        // Validate robot A if provided
        if (matchObj.getRobotAID() != null) {
            Optional<Robot> robotAOpt = this.robotRepository.findById(matchObj.getRobotAID());
            if (!robotAOpt.isPresent()) {
                throw new Exception(
                        String.format("failure, robot with ID [%d] not exists", matchObj.getRobotAID()));
            }
            robotA = robotAOpt.get();

            // Check if competition has started
            Competition competition = robotA.getTeamRegistration().getCompetition();
            if (!competition.getStarted()) {
                throw new Exception(
                        String.format("failure, competition year [%d] has not started yet. Matches cannot be created before the competition starts.",
                                competition.getYear()));
            }

            // Verify robot is confirmed
            if (!robotA.getConfirmed()) {
                throw new Exception(
                        String.format("failure, robot with ID [%d] is not confirmed", matchObj.getRobotAID()));
            }

            // Check max rounds limit
            int maxRounds = robotA.getDiscipline().getMaxRounds();
            if (maxRounds >= 0) {
                if (robotA.getMatches().size() >= maxRounds) {
                    throw new Exception(
                            String.format("failure, robot with ID [%d] exceeded the maximum number of matches",
                                    matchObj.getRobotAID()));
                }
            }
        }

        // Validate robot B if provided
        if (matchObj.getRobotBID() != null) {
            Optional<Robot> robotBOpt = this.robotRepository.findById(matchObj.getRobotBID());
            if (!robotBOpt.isPresent()) {
                throw new Exception(
                        String.format("failure, robot with ID [%d] not exists", matchObj.getRobotBID()));
            }
            robotB = robotBOpt.get();

            // Verify robot is confirmed
            if (!robotB.getConfirmed()) {
                throw new Exception(
                        String.format("failure, robot with ID [%d] is not confirmed", matchObj.getRobotBID()));
            }

            // Verify both robots are from the same category (if both are assigned)
            if (robotA != null && robotA.getCategory() != robotB.getCategory()) {
                throw new Exception("failure, robots must be from the same category");
            }

            // Verify both robots are from the same discipline
            if (robotA != null && !robotA.getDiscipline().getID().equals(robotB.getDiscipline().getID())) {
                throw new Exception("failure, robots must be from the same discipline");
            }

            // Check max rounds limit for robot B
            int maxRounds = robotB.getDiscipline().getMaxRounds();
            if (maxRounds >= 0) {
                if (robotB.getMatches().size() >= maxRounds) {
                    throw new Exception(
                            String.format("failure, robot with ID [%d] exceeded the maximum number of matches",
                                    matchObj.getRobotBID()));
                }
            }
        }

        // Get tournament phase if provided
        TournamentPhase phase = null;
        if (matchObj.getPhase() != null) {
            Optional<TournamentPhase> phaseOpt = this.tournamentPhaseRepository.findByName(matchObj.getPhase());
            if (phaseOpt.isPresent()) {
                phase = phaseOpt.get();
            }
        }

        // Get next match if provided
        RobotMatch nextMatch = null;
        if (matchObj.getNextMatchID() != null) {
            Optional<RobotMatch> nextMatchOpt = this.robotMatchRepository.findById(matchObj.getNextMatchID());
            if (!nextMatchOpt.isPresent()) {
                throw new Exception(
                        String.format("failure, next match with ID [%d] not exists", matchObj.getNextMatchID()));
            }
            nextMatch = nextMatchOpt.get();
        }

        // Get initial match state
        MatchState state = matchStateRepository.findByName(EMatchState.WAITING).get();

        // Get score type from discipline
        ScoreType scoreType = discipline.getScoreType();

        // Determine highScoreWin - use provided value or inherit from discipline
        boolean highScoreWin = matchObj.getHighScoreWin() != null 
                ? matchObj.getHighScoreWin() 
                : (discipline.getHighScoreWin() != null ? discipline.getHighScoreWin() : true);

        // Create and save the match
        RobotMatch match = new RobotMatch(robotA, robotB, playground.get(), state,
                scoreType, phase, nextMatch, highScoreWin);
        
        // Set group and visual position if provided
        if (matchObj.getGroup() != null) {
            match.setGroup(matchObj.getGroup());
        }
        if (matchObj.getVisualX() != null) {
            match.setVisualX(matchObj.getVisualX());
        }
        if (matchObj.getVisualY() != null) {
            match.setVisualY(matchObj.getVisualY());
        }
        
        // Set competition year if provided (for matches without robots)
        if (matchObj.getCompetitionYear() != null) {
            match.setCompetitionYear(matchObj.getCompetitionYear());
        }
        
        this.robotMatchRepository.save(match);

        // Send message to communication system
        Communication.getInstance().sendAll(this, MatchService.Message.CREATE);

        return match;
    }

    /**
     * Update an existing match
     * 
     * @param id Match ID
     * @param matchObj New match parameters
     * @throws Exception on validation errors
     */
    public void update(Long id, RobotMatchObj matchObj) throws Exception {
        Optional<RobotMatch> matchOpt = this.robotMatchRepository.findById(id);
        if (!matchOpt.isPresent()) {
            throw new Exception(String.format("failure, match with ID [%d] not exists", id));
        }

        RobotMatch match = matchOpt.get();

        // Update playground if provided
        if (matchObj.getPlaygroundID() != null) {
            Optional<Playground> playground = this.playgroundRepository.findById(matchObj.getPlaygroundID());
            if (!playground.isPresent()) {
                throw new Exception(
                        String.format("failure, playground with ID [%d] not exists", matchObj.getPlaygroundID()));
            }
            match.setPlayground(playground.get());
        }

        // Update robot A if provided (0 means remove robot)
        if (matchObj.getRobotAID() != null) {
            if (matchObj.getRobotAID() == 0) {
                match.setRobotA(null);
            } else {
                Optional<Robot> robotA = this.robotRepository.findById(matchObj.getRobotAID());
                if (!robotA.isPresent()) {
                    throw new Exception(
                            String.format("failure, robot with ID [%d] not exists", matchObj.getRobotAID()));
                }
                if (!robotA.get().getConfirmed()) {
                    throw new Exception(
                            String.format("failure, robot with ID [%d] is not confirmed", matchObj.getRobotAID()));
                }
                match.setRobotA(robotA.get());
            }
        }

        // Update robot B if provided (0 means remove robot)
        if (matchObj.getRobotBID() != null) {
            if (matchObj.getRobotBID() == 0) {
                match.setRobotB(null);
            } else {
                Optional<Robot> robotB = this.robotRepository.findById(matchObj.getRobotBID());
                if (!robotB.isPresent()) {
                    throw new Exception(
                            String.format("failure, robot with ID [%d] not exists", matchObj.getRobotBID()));
                }
                if (!robotB.get().getConfirmed()) {
                    throw new Exception(
                            String.format("failure, robot with ID [%d] is not confirmed", matchObj.getRobotBID()));
                }
                match.setRobotB(robotB.get());
            }
        }

        // Update tournament phase if provided
        if (matchObj.getPhase() != null) {
            Optional<TournamentPhase> phase = this.tournamentPhaseRepository.findByName(matchObj.getPhase());
            if (phase.isPresent()) {
                match.setPhase(phase.get());
            }
        }

        // Update next match if provided
        if (matchObj.getNextMatchID() != null) {
            Optional<RobotMatch> nextMatch = this.robotMatchRepository.findById(matchObj.getNextMatchID());
            if (!nextMatch.isPresent()) {
                throw new Exception(
                        String.format("failure, next match with ID [%d] not exists", matchObj.getNextMatchID()));
            }
            match.setNextMatch(nextMatch.get());
        }

        // Update highScoreWin if provided
        if (matchObj.getHighScoreWin() != null) {
            match.setHighScoreWin(matchObj.getHighScoreWin());
        }

        // Update group if provided (can be set to null to remove from group)
        if (matchObj.getGroup() != null) {
            match.setGroup(matchObj.getGroup());
        }

        // Update visual position if provided
        if (matchObj.getVisualX() != null) {
            match.setVisualX(matchObj.getVisualX());
        }
        if (matchObj.getVisualY() != null) {
            match.setVisualY(matchObj.getVisualY());
        }

        this.robotMatchRepository.save(match);

        // Send message to communication system
        Communication.getInstance().sendAll(this, MatchService.Message.UPDATE);
    }

    /**
     * Assign robots to an existing match
     * 
     * @param id Match ID
     * @param robotAID Robot A ID (can be null to keep existing)
     * @param robotBID Robot B ID (can be null to keep existing)
     * @throws Exception on validation errors
     */
    public void assignRobots(Long id, Long robotAID, Long robotBID) throws Exception {
        Optional<RobotMatch> matchOpt = this.robotMatchRepository.findById(id);
        if (!matchOpt.isPresent()) {
            throw new Exception(String.format("failure, match with ID [%d] not exists", id));
        }

        RobotMatch match = matchOpt.get();

        if (robotAID != null) {
            Optional<Robot> robotA = this.robotRepository.findById(robotAID);
            if (!robotA.isPresent()) {
                throw new Exception(String.format("failure, robot with ID [%d] not exists", robotAID));
            }
            if (!robotA.get().getConfirmed()) {
                throw new Exception(String.format("failure, robot with ID [%d] is not confirmed", robotAID));
            }
            match.setRobotA(robotA.get());
        }

        if (robotBID != null) {
            Optional<Robot> robotB = this.robotRepository.findById(robotBID);
            if (!robotB.isPresent()) {
                throw new Exception(String.format("failure, robot with ID [%d] not exists", robotBID));
            }
            if (!robotB.get().getConfirmed()) {
                throw new Exception(String.format("failure, robot with ID [%d] is not confirmed", robotBID));
            }
            // Verify robots are from same category
            if (match.getRobotA() != null && match.getRobotA().getCategory() != robotB.get().getCategory()) {
                throw new Exception("failure, robots must be from the same category");
            }
            match.setRobotB(robotB.get());
        }

        this.robotMatchRepository.save(match);

        // Send message to communication system
        Communication.getInstance().sendAll(this, MatchService.Message.UPDATE);
    }

    /**
     * Remove a match
     * 
     * @param id Match ID
     * @throws Exception if match not found
     */
    public void remove(Long id) throws Exception {
        if (!this.robotMatchRepository.findById(id).isPresent()) {
            throw new Exception(String.format("failure, match with ID [%d] not exists", id));
        }

        this.robotMatchRepository.deleteById(id);

        // Send message to communication system
        Communication.getInstance().sendAll(this, MatchService.Message.REMOVE);
    }

    /**
     * Write scores for a match and automatically mark it as done
     * Also handles progression to next match in bracket-style tournaments
     * 
     * @param scoreObj Score data
     * @throws Exception on validation errors
     */
    public void writeScore(MatchScoreObj scoreObj) throws Exception {
        Optional<RobotMatch> matchOpt = this.robotMatchRepository.findById(scoreObj.getMatchID());
        if (!matchOpt.isPresent()) {
            throw new Exception(String.format("failure, match with ID [%d] not exists", scoreObj.getMatchID()));
        }

        RobotMatch match = matchOpt.get();

        // Validate that robots are assigned
        if (match.getRobotA() == null) {
            throw new Exception("failure, cannot write score - no robots assigned to this match");
        }

        // Set scores
        match.setScoreA(scoreObj.getScoreA());
        
        // Only set score B if it's a two-robot match
        if (match.getRobotB() != null) {
            if (scoreObj.getScoreB() == null) {
                throw new Exception("failure, score B is required for two-robot matches");
            }
            match.setScoreB(scoreObj.getScoreB());
        }

        // Mark as done
        MatchState doneState = matchStateRepository.findByName(EMatchState.DONE).get();
        match.setMatchState(doneState);

        this.robotMatchRepository.save(match);

        // Handle progression to next match if configured
        if (match.getNextMatch() != null) {
            this.progressWinnerToNextMatch(match);
        }

        // Send message to communication system
        Communication.getInstance().sendAll(this, MatchService.Message.WRITE_SCORE);
    }

    /**
     * Request a rematch - reset scores and mark for replay
     * 
     * @param id Match ID
     * @throws Exception if match not found
     */
    public void rematch(Long id) throws Exception {
        Optional<RobotMatch> matchOpt = this.robotMatchRepository.findById(id);
        if (!matchOpt.isPresent()) {
            throw new Exception(String.format("failure, match with ID [%d] not exists", id));
        }

        RobotMatch match = matchOpt.get();

        // Reset scores
        match.setScoreA(null);
        match.setScoreB(null);

        // Set rematch state
        MatchState rematchState = matchStateRepository.findByName(EMatchState.REMATCH).get();
        match.setMatchState(rematchState);

        this.robotMatchRepository.save(match);

        // Send message to communication system
        Communication.getInstance().sendAll(this, MatchService.Message.REMATCH);
    }

    /**
     * Progress the winner of a match to the next match in bracket
     * Prevents duplicate assignments to both robot A and robot B positions
     * 
     * @param match The completed match
     */
    private void progressWinnerToNextMatch(RobotMatch match) {
        Robot winner = match.getWinner();
        if (winner == null) {
            return; // No clear winner (tie or missing scores)
        }

        RobotMatch nextMatch = match.getNextMatch();
        if (nextMatch == null) {
            return;
        }

        // Check if winner is already assigned to next match
        if (nextMatch.getRobotA() != null && nextMatch.getRobotA().getID().equals(winner.getID())) {
            return; // Already assigned as robot A
        }
        if (nextMatch.getRobotB() != null && nextMatch.getRobotB().getID().equals(winner.getID())) {
            return; // Already assigned as robot B
        }

        // Check if the loser from this match is already in the next match
        // This prevents both robots from the same match going to next match
        Robot loser = (winner.equals(match.getRobotA())) ? match.getRobotB() : match.getRobotA();
        if (loser != null) {
            if (nextMatch.getRobotA() != null && nextMatch.getRobotA().getID().equals(loser.getID())) {
                return; // Loser already in next match - something is wrong
            }
            if (nextMatch.getRobotB() != null && nextMatch.getRobotB().getID().equals(loser.getID())) {
                return; // Loser already in next match - something is wrong
            }
        }

        // Assign winner to first available slot
        if (nextMatch.getRobotA() == null) {
            nextMatch.setRobotA(winner);
        } else if (nextMatch.getRobotB() == null) {
            nextMatch.setRobotB(winner);
        }

        this.robotMatchRepository.save(nextMatch);
    }

    /**
     * Get all waiting/scheduled matches for a playground
     * 
     * @param playgroundID Playground ID
     * @return List of waiting matches
     */
    public List<RobotMatch> getWaitingMatches(Long playgroundID) throws Exception {
        Optional<Playground> playground = this.playgroundRepository.findById(playgroundID);
        if (!playground.isPresent()) {
            throw new Exception(String.format("failure, playground with ID [%d] not exists", playgroundID));
        }

        return this.robotMatchRepository.findAll().stream()
                .filter(m -> m.getPlaygroundID().equals(playgroundID))
                .filter(m -> m.getState().getName() == EMatchState.WAITING 
                          || m.getState().getName() == EMatchState.REMATCH)
                .collect(Collectors.toList());
    }

    /**
     * Get matches by tournament phase
     * 
     * @param phase Tournament phase
     * @return List of matches in that phase
     */
    public List<RobotMatch> getByPhase(ETournamentPhase phase) {
        return this.robotMatchRepository.findAll().stream()
                .filter(m -> m.getPhaseName() == phase)
                .collect(Collectors.toList());
    }

}
