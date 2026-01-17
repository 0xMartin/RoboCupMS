package com.robogames.RoboCupMS.Module.CompetitionEvaluation.Bussiness.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.robogames.RoboCupMS.Business.Enum.ECategory;
import com.robogames.RoboCupMS.Business.Enum.EMatchState;
import com.robogames.RoboCupMS.Business.Enum.EScoreAggregation;
import com.robogames.RoboCupMS.Business.Enum.ETournamentPhase;
import com.robogames.RoboCupMS.Business.Object.RobotMatchInfo;
import com.robogames.RoboCupMS.Entity.Competition;
import com.robogames.RoboCupMS.Entity.Discipline;
import com.robogames.RoboCupMS.Entity.Robot;
import com.robogames.RoboCupMS.Entity.RobotMatch;
import com.robogames.RoboCupMS.Entity.ScoreAggregation;
import com.robogames.RoboCupMS.Entity.Team;
import com.robogames.RoboCupMS.Entity.TeamRegistration;
import com.robogames.RoboCupMS.Module.CompetitionEvaluation.Bussiness.Object.OrderObj;
import com.robogames.RoboCupMS.Module.CompetitionEvaluation.Bussiness.Object.RobotScore;
import com.robogames.RoboCupMS.Module.CompetitionEvaluation.Bussiness.Object.TeamScore;
import com.robogames.RoboCupMS.Repository.CompetitionRepository;
import com.robogames.RoboCupMS.Repository.DisciplineRepository;
import com.robogames.RoboCupMS.Repository.RobotRepository;
import com.robogames.RoboCupMS.Repository.TeamRepository;
import com.robogames.RoboCupMS.Repository.RobotMatchRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompetitionEvaluationService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private RobotRepository robotRepository;

    @Autowired
    private DisciplineRepository disciplineRepository;

    @Autowired
    private RobotMatchRepository robotMatchRepository;

    /**
     * Returns the score for a robot in a specific match.
     * For single-robot matches (robotB is null), returns scoreA.
     * For two-robot matches, returns the score of the requested robot.
     * 
     * @param match The match
     * @param robot The robot to get the score for
     * @return The score for the robot, or null if not found
     */
    private Float getScoreForRobot(RobotMatch match, Robot robot) {
        if (match.getRobotA() != null && match.getRobotA().getID() == robot.getID()) {
            return match.getScoreA();
        }
        if (match.getRobotB() != null && match.getRobotB().getID() == robot.getID()) {
            return match.getScoreB();
        }
        return null;
    }

    /**
     * Returns the score of all robots that competed in the given year
     * 
     * @param year     Competition year
     * @param category Category for which to show results
     * @return List of all robots and their scores in the competition
     */
    public List<RobotScore> getScoreOfAll(int year, ECategory category) throws Exception {
        // verify that competition year exists
        Optional<Competition> competition = this.competitionRepository.findByYear(year);
        if (!competition.isPresent()) {
            throw new Exception(String.format("failure, competition [%d] not exists", year));
        }

        // for all registered teams, calculate total score for all their confirmed robots
        List<RobotScore> scoreList = new LinkedList<RobotScore>();

        List<TeamRegistration> registrations = competition.get().getRegistrations();
        for (TeamRegistration reg : registrations) {
            List<Robot> robots = reg.getRobots();
            for (Robot r : robots) {
                if (!r.getConfirmed() || r.getCategory() != category)
                    continue;

                // score aggregation function
                ScoreAggregation ag = r.getDiscipline().getScoreAggregation();
                // apply function for all matches the robot played
                float totalScore = ag.getTotalScoreInitValue();
                List<RobotMatch> matches = r.getMatches();
                for (RobotMatch m : matches) {
                    if (m.getState().getName() == EMatchState.DONE) {
                        Float robotScore = getScoreForRobot(m, r);
                        if (robotScore != null) {
                            totalScore = ag.proccess(totalScore, robotScore);
                        }
                    }
                }

                // record score with robot
                scoreList.add(new RobotScore(r, totalScore));
            }
        }

        List<RobotScore> minScores = filterByAggregation(scoreList, EScoreAggregation.MIN);
        List<RobotScore> maxScores = filterByAggregation(scoreList, EScoreAggregation.MAX);
        List<RobotScore> sumScores = filterByAggregation(scoreList, EScoreAggregation.SUM);

        sortListByCriteria(minScores, EScoreAggregation.MIN);
        sortListByCriteria(maxScores, EScoreAggregation.MAX);
        sortListByCriteria(sumScores, EScoreAggregation.SUM);

        return mergeSortedLists(minScores, maxScores, sumScores);
    }

    /**
     * Returns the score of all robots of a specific team
     * 
     * @param year Year
     * @param id   Team ID
     * @return Returns the score of all robots in the team
     */
    public TeamScore getScoreOfTeam(int year, long id) throws Exception {
        // verify that team exists
        Optional<Team> team = this.teamRepository.findById(id);
        if (!team.isPresent()) {
            throw new Exception(String.format("failure, team with ID [%d] not exists", id));
        }

        // find registration for the given competition year
        Optional<TeamRegistration> registration = team.get().getRegistrations().stream()
                .filter((r) -> (r.getCompetitionYear() == year)).findFirst();
        if (!registration.isPresent()) {
            throw new Exception(String.format("failure, registration for year [%d] not exists", year));
        }

        // calculate score for each robot
        List<RobotScore> scoreList = new ArrayList<RobotScore>();
        List<Robot> robots = registration.get().getRobots();

        robots.stream().forEach((r) -> {
            // robot registration must be confirmed
            if (r.getConfirmed()) {
                // score aggregation function
                ScoreAggregation ag = r.getDiscipline().getScoreAggregation();
                // apply function for all matches the robot played
                float totalScore = ag.getTotalScoreInitValue();
                List<RobotMatch> matches = r.getMatches();
                for (RobotMatch m : matches) {
                    if (m.getState().getName() == EMatchState.DONE) {
                        Float robotScore = getScoreForRobot(m, r);
                        if (robotScore != null) {
                            totalScore = ag.proccess(totalScore, robotScore);
                        }
                    }
                }
                scoreList.add(new RobotScore(r, totalScore));
            }
        });

        return new TeamScore(team.get(), scoreList);
    }

    /**
     * Returns the score of one specific robot
     * 
     * @param year Competition year
     * @param id   Robot ID
     * @return Returns the robot's score
     */
    public RobotScore getScoreOfRobot(int year, long id) throws Exception {
        Optional<Robot> robot = this.robotRepository.findById(id);
        if (!robot.isPresent()) {
            throw new Exception(String.format("failure, robot with ID [%d] not exists", id));
        }

        // verify robot confirmation
        if (!robot.get().getConfirmed()) {
            throw new Exception(String.format("failure, robot with ID [%d] is not confirmed", id));
        }

        // verify competition year
        if (robot.get().getTeamRegistration().getCompetitionYear() != year) {
            throw new Exception(String.format("failure, this robot is not registed in year [%d]", year));
        }

        Robot r = robot.get();
        
        // score aggregation function
        ScoreAggregation ag = r.getDiscipline().getScoreAggregation();
        // apply function for all matches the robot played
        float totalScore = ag.getTotalScoreInitValue();
        List<RobotMatch> matches = r.getMatches();
        for (RobotMatch m : matches) {
            if (m.getState().getName() == EMatchState.DONE) {
                Float robotScore = getScoreForRobot(m, r);
                if (robotScore != null) {
                    totalScore = ag.proccess(totalScore, robotScore);
                }
            }
        }

        return new RobotScore(r, totalScore);
    }

    /**
     * Returns the placement of robots in a specific discipline within a competition category.
     * For tournament-type disciplines (with bracket), the placement is determined by bracket results:
     * - 1st place: Winner of FINAL match
     * - 2nd place: Loser of FINAL match
     * - 3rd place: Winner of THIRD_PLACE match
     * - 4th place: Loser of THIRD_PLACE match
     * - 5th-8th: Losers of SEMIFINAL (sorted by score)
     * - 9th-16th: Losers of QUARTERFINAL (sorted by score)
     * - etc. for earlier rounds
     * - Last: Robots that didn't advance from groups (sorted by group standings)
     * 
     * For regular disciplines, placement is based on total score from all matches.
     * 
     * @param year     Competition year
     * @param category Competition category
     * @param id       Discipline ID
     * @return Order of all robots that competed in the given discipline + category
     */
    public List<OrderObj> getOrder(int year, ECategory category, long id) throws Exception {
        // verify that competition year exists
        if (!this.competitionRepository.findByYear(year).isPresent()) {
            throw new Exception(String.format("failure, competition [%d] not exists", year));
        }

        // find discipline
        Optional<Discipline> discipline = this.disciplineRepository.findById(id);
        if (!discipline.isPresent()) {
            throw new Exception(String.format("failure, discipline with ID [%d] not exists", id));
        }

        // Get all matches for this discipline
        List<RobotMatch> allMatches = robotMatchRepository.findAll().stream()
            .filter(m -> m.getPlayground() != null && 
                        m.getPlayground().getDiscipline().getID() == id)
            .collect(Collectors.toList());

        // Check if this is a tournament (has bracket matches)
        String disciplineShortName = getDisciplineShortName(discipline.get());
        String categoryShortCode = category == ECategory.LOW_AGE_CATEGORY ? "L" : "H";
        String bracketId = String.format("%s_%s_%d_BR", disciplineShortName, categoryShortCode, year);
        
        List<RobotMatch> bracketMatches = allMatches.stream()
            .filter(m -> m.getGroup() != null && m.getGroup().equals(bracketId))
            .collect(Collectors.toList());

        boolean isTournament = !bracketMatches.isEmpty();

        if (isTournament) {
            return getOrderForTournament(year, category, discipline.get(), allMatches, bracketMatches, bracketId);
        } else {
            return getOrderForRegularDiscipline(year, category, discipline.get());
        }
    }

    /**
     * Calculate order for tournament-type disciplines based on bracket results
     */
    private List<OrderObj> getOrderForTournament(int year, ECategory category, Discipline discipline,
            List<RobotMatch> allMatches, List<RobotMatch> bracketMatches, String bracketId) {
        
        Boolean highScoreWin = discipline.getHighScoreWin();
        List<OrderObj> order = new ArrayList<>();
        int currentPlace = 1;

        // Track robots that have been placed
        java.util.Set<Long> placedRobots = new java.util.HashSet<>();

        // 1. Find FINAL match - winner is 1st, loser is 2nd
        List<RobotMatch> finalMatches = bracketMatches.stream()
            .filter(m -> m.getPhase() != null && m.getPhase().getName() == ETournamentPhase.FINAL)
            .filter(m -> m.getState().getName() == EMatchState.DONE)
            .collect(Collectors.toList());

        for (RobotMatch finalMatch : finalMatches) {
            Robot winner = getMatchWinner(finalMatch, highScoreWin);
            Robot loser = getMatchLoser(finalMatch, highScoreWin);
            
            if (winner != null && !placedRobots.contains(winner.getID())) {
                order.add(createOrderObj(currentPlace++, winner, finalMatch, highScoreWin));
                placedRobots.add(winner.getID());
            }
            if (loser != null && !placedRobots.contains(loser.getID())) {
                order.add(createOrderObj(currentPlace++, loser, finalMatch, highScoreWin));
                placedRobots.add(loser.getID());
            }
        }

        // 2. Find THIRD_PLACE match - winner is 3rd, loser is 4th
        List<RobotMatch> thirdPlaceMatches = bracketMatches.stream()
            .filter(m -> m.getPhase() != null && m.getPhase().getName() == ETournamentPhase.THIRD_PLACE)
            .filter(m -> m.getState().getName() == EMatchState.DONE)
            .collect(Collectors.toList());

        for (RobotMatch thirdMatch : thirdPlaceMatches) {
            Robot winner = getMatchWinner(thirdMatch, highScoreWin);
            Robot loser = getMatchLoser(thirdMatch, highScoreWin);
            
            if (winner != null && !placedRobots.contains(winner.getID())) {
                order.add(createOrderObj(currentPlace++, winner, thirdMatch, highScoreWin));
                placedRobots.add(winner.getID());
            }
            if (loser != null && !placedRobots.contains(loser.getID())) {
                order.add(createOrderObj(currentPlace++, loser, thirdMatch, highScoreWin));
                placedRobots.add(loser.getID());
            }
        }

        // 3. Process remaining bracket phases in order: SEMIFINAL, QUARTERFINAL, ROUND_OF_16, PRELIMINARY
        ETournamentPhase[] phases = { 
            ETournamentPhase.SEMIFINAL, 
            ETournamentPhase.QUARTERFINAL, 
            ETournamentPhase.ROUND_OF_16,
            ETournamentPhase.PRELIMINARY 
        };

        for (ETournamentPhase phase : phases) {
            List<Robot> losersInPhase = new ArrayList<>();
            
            List<RobotMatch> phaseMatches = bracketMatches.stream()
                .filter(m -> m.getPhase() != null && m.getPhase().getName() == phase)
                .filter(m -> m.getState().getName() == EMatchState.DONE)
                .collect(Collectors.toList());

            for (RobotMatch match : phaseMatches) {
                Robot loser = getMatchLoser(match, highScoreWin);
                if (loser != null && !placedRobots.contains(loser.getID())) {
                    losersInPhase.add(loser);
                    placedRobots.add(loser.getID());
                }
            }

            // Sort losers by their score in the match they lost (higher score = better placement)
            // This creates a fair sub-ordering within the same bracket round
            losersInPhase.sort((a, b) -> {
                Float scoreA = getLastMatchScore(a, bracketMatches);
                Float scoreB = getLastMatchScore(b, bracketMatches);
                if (highScoreWin != null && highScoreWin) {
                    return Float.compare(scoreB != null ? scoreB : 0, scoreA != null ? scoreA : 0);
                } else {
                    return Float.compare(scoreA != null ? scoreA : Float.MAX_VALUE, scoreB != null ? scoreB : Float.MAX_VALUE);
                }
            });

            for (Robot loser : losersInPhase) {
                order.add(createOrderObjFromRobot(currentPlace++, loser, discipline));
            }
        }

        // 4. Add robots that didn't advance from groups
        String disciplineShortName = getDisciplineShortName(discipline);
        String categoryShortCode = category == ECategory.LOW_AGE_CATEGORY ? "L" : "H";
        String groupPrefix = String.format("%s_%s_%d_", disciplineShortName, categoryShortCode, year);

        List<RobotMatch> groupMatches = allMatches.stream()
            .filter(m -> m.getGroup() != null && m.getGroup().startsWith(groupPrefix) && !m.getGroup().endsWith("_BR"))
            .collect(Collectors.toList());

        // Get all robots from groups
        java.util.Set<Long> groupRobots = new java.util.HashSet<>();
        for (RobotMatch match : groupMatches) {
            if (match.getRobotA() != null) groupRobots.add(match.getRobotA().getID());
            if (match.getRobotB() != null) groupRobots.add(match.getRobotB().getID());
        }

        // Find robots that played in groups but aren't placed yet
        List<Robot> unplacedGroupRobots = new ArrayList<>();
        for (Long robotId : groupRobots) {
            if (!placedRobots.contains(robotId)) {
                Optional<Robot> robot = robotRepository.findById(robotId);
                if (robot.isPresent()) {
                    unplacedGroupRobots.add(robot.get());
                    placedRobots.add(robotId);
                }
            }
        }

        // Sort unplaced robots by their group performance (wins, then score diff)
        Map<Long, Map<String, Object>> robotGroupStats = calculateGroupStatsForRobots(groupMatches, highScoreWin);
        unplacedGroupRobots.sort((a, b) -> {
            Map<String, Object> statsA = robotGroupStats.get(a.getID());
            Map<String, Object> statsB = robotGroupStats.get(b.getID());
            if (statsA == null && statsB == null) return 0;
            if (statsA == null) return 1;
            if (statsB == null) return -1;
            int winsComp = Integer.compare((int) statsB.get("wins"), (int) statsA.get("wins"));
            if (winsComp != 0) return winsComp;
            return Float.compare((float) statsB.get("scoreDiff"), (float) statsA.get("scoreDiff"));
        });

        for (Robot robot : unplacedGroupRobots) {
            order.add(createOrderObjFromRobot(currentPlace++, robot, discipline));
        }

        // 5. Add any remaining robots in this discipline/category that weren't in matches yet
        Stream<Robot> allRobots = discipline.getRobots().stream()
            .filter(r -> r.getTeamRegistration().getCompetitionYear() == year)
            .filter(r -> r.getConfirmed() && r.getCategory() == category)
            .filter(r -> !placedRobots.contains(r.getID()));

        List<Robot> remainingRobots = allRobots.collect(Collectors.toList());
        for (Robot robot : remainingRobots) {
            order.add(createOrderObjFromRobot(currentPlace++, robot, discipline));
        }

        return order;
    }

    /**
     * Calculate order for regular (non-tournament) disciplines based on total score
     */
    private List<OrderObj> getOrderForRegularDiscipline(int year, ECategory category, Discipline discipline) {
        // find all robots in discipline who played in the given year
        Stream<Robot> robots = discipline.getRobots().stream()
                .filter((r) -> (r.getTeamRegistration().getCompetitionYear() == year));

        // score aggregation function
        ScoreAggregation ag = discipline.getScoreAggregation();

        List<RobotScore> all = new LinkedList<>();
        robots.forEach(r -> {
            if (r.getConfirmed() && r.getCategory() == category) {
                // apply function for all matches the robot played
                float totalScore = ag.getTotalScoreInitValue();
                List<RobotMatch> matches = r.getMatches();
                for (RobotMatch m : matches) {
                    if (m.getState().getName() == EMatchState.DONE) {
                        Float robotScore = getScoreForRobot(m, r);
                        if (robotScore != null) {
                            totalScore = ag.proccess(totalScore, robotScore);
                        }
                    }
                }

                // record score with robot
                all.add(new RobotScore(r, totalScore));
            }
        });

        // sorting
        if (ag.getName() == EScoreAggregation.MIN) {
            Collections.sort(all, (r1, r2) -> r2.getScore() < r1.getScore() ? 1 : -1);
        } else {
            Collections.sort(all, (r1, r2) -> r2.getScore() > r1.getScore() ? 1 : -1);
        }

        List<OrderObj> order = new LinkedList<>();
        int place = 1;
        for (RobotScore scoreObj : all) {
            order.add(new OrderObj(place++, scoreObj));
        }

        return order;
    }

    /**
     * Get winner of a match based on scores
     */
    private Robot getMatchWinner(RobotMatch match, Boolean highScoreWin) {
        if (match.getRobotA() == null || match.getRobotB() == null) return null;
        if (match.getScoreA() == null || match.getScoreB() == null) return null;
        
        boolean aWins;
        if (highScoreWin != null && highScoreWin) {
            aWins = match.getScoreA() > match.getScoreB();
        } else {
            aWins = match.getScoreA() < match.getScoreB();
        }
        
        return aWins ? match.getRobotA() : match.getRobotB();
    }

    /**
     * Get loser of a match based on scores
     */
    private Robot getMatchLoser(RobotMatch match, Boolean highScoreWin) {
        if (match.getRobotA() == null || match.getRobotB() == null) return null;
        if (match.getScoreA() == null || match.getScoreB() == null) return null;
        
        boolean aWins;
        if (highScoreWin != null && highScoreWin) {
            aWins = match.getScoreA() > match.getScoreB();
        } else {
            aWins = match.getScoreA() < match.getScoreB();
        }
        
        return aWins ? match.getRobotB() : match.getRobotA();
    }

    /**
     * Get robot's score from their last match in the bracket
     */
    private Float getLastMatchScore(Robot robot, List<RobotMatch> bracketMatches) {
        for (RobotMatch match : bracketMatches) {
            if (match.getState().getName() != EMatchState.DONE) continue;
            if (match.getRobotA() != null && match.getRobotA().getID() == robot.getID()) {
                return match.getScoreA();
            }
            if (match.getRobotB() != null && match.getRobotB().getID() == robot.getID()) {
                return match.getScoreB();
            }
        }
        return null;
    }

    /**
     * Calculate group statistics for all robots
     */
    private Map<Long, Map<String, Object>> calculateGroupStatsForRobots(List<RobotMatch> groupMatches, Boolean highScoreWin) {
        Map<Long, Map<String, Object>> robotStats = new HashMap<>();

        for (RobotMatch match : groupMatches) {
            if (match.getState().getName() != EMatchState.DONE) continue;

            Robot robotA = match.getRobotA();
            Robot robotB = match.getRobotB();
            Float scoreA = match.getScoreA();
            Float scoreB = match.getScoreB();

            if (robotA == null || robotB == null || scoreA == null || scoreB == null) continue;

            // Initialize robot stats if needed
            if (!robotStats.containsKey(robotA.getID())) {
                Map<String, Object> stats = new HashMap<>();
                stats.put("wins", 0);
                stats.put("losses", 0);
                stats.put("scoreDiff", 0f);
                robotStats.put(robotA.getID(), stats);
            }
            if (!robotStats.containsKey(robotB.getID())) {
                Map<String, Object> stats = new HashMap<>();
                stats.put("wins", 0);
                stats.put("losses", 0);
                stats.put("scoreDiff", 0f);
                robotStats.put(robotB.getID(), stats);
            }

            // Determine winner
            boolean aWins;
            if (highScoreWin != null && highScoreWin) {
                aWins = scoreA > scoreB;
            } else {
                aWins = scoreA < scoreB;
            }

            Map<String, Object> statsA = robotStats.get(robotA.getID());
            Map<String, Object> statsB = robotStats.get(robotB.getID());

            if (aWins) {
                statsA.put("wins", (int) statsA.get("wins") + 1);
                statsB.put("losses", (int) statsB.get("losses") + 1);
            } else if (!scoreA.equals(scoreB)) {
                statsB.put("wins", (int) statsB.get("wins") + 1);
                statsA.put("losses", (int) statsA.get("losses") + 1);
            }

            statsA.put("scoreDiff", (float) statsA.get("scoreDiff") + (scoreA - scoreB));
            statsB.put("scoreDiff", (float) statsB.get("scoreDiff") + (scoreB - scoreA));
        }

        return robotStats;
    }

    /**
     * Create OrderObj from robot and match info
     */
    private OrderObj createOrderObj(int place, Robot robot, RobotMatch match, Boolean highScoreWin) {
        Float score = getScoreForRobot(match, robot);
        return new OrderObj(place, new RobotScore(robot, score != null ? score : 0));
    }

    /**
     * Create OrderObj from robot with total score from all matches
     */
    private OrderObj createOrderObjFromRobot(int place, Robot robot, Discipline discipline) {
        ScoreAggregation ag = discipline.getScoreAggregation();
        float totalScore = ag.getTotalScoreInitValue();
        List<RobotMatch> matches = robot.getMatches();
        for (RobotMatch m : matches) {
            if (m.getState().getName() == EMatchState.DONE) {
                Float robotScore = getScoreForRobot(m, robot);
                if (robotScore != null) {
                    totalScore = ag.proccess(totalScore, robotScore);
                }
            }
        }
        return new OrderObj(place, new RobotScore(robot, totalScore));
    }

    private static List<RobotScore> filterByAggregation(List<RobotScore> scoreList, EScoreAggregation aggregation) {
        List<RobotScore> filteredList = new ArrayList<>();
        for (RobotScore rs : scoreList) {
            if (rs.getRobot().getDiscipline().getScoreAggregation().getName() == aggregation) {
                filteredList.add(rs);
            }
        }
        return filteredList;
    }

    private static void sortListByCriteria(List<RobotScore> scoreList, EScoreAggregation aggregation) {
        // sorting
        if (aggregation == EScoreAggregation.MIN) {
            // MIN -> from lowest score to highest (line follower, drag race, ...
            // => score represents time)
            Collections.sort(scoreList, new Comparator<RobotScore>() {
                @Override
                public int compare(RobotScore r1, RobotScore r2) {
                    return r2.getScore() < r1.getScore() ? 1 : -1;
                }
            });
        } else {
            // MAX or SUM -> from highest to lowest (sumo, robostrong, ..)
            Collections.sort(scoreList, new Comparator<RobotScore>() {
                @Override
                public int compare(RobotScore r1, RobotScore r2) {
                    return r2.getScore() > r1.getScore() ? 1 : -1;
                }
            });
        }
    }

    private static List<RobotScore> mergeSortedLists(List<RobotScore> minList, List<RobotScore> maxList,
            List<RobotScore> sumList) {
        List<RobotScore> mergedList = new ArrayList<>();
        int size = Math.max(Math.max(minList.size(), maxList.size()), sumList.size());
        for (int i = 0; i < size; i++) {
            if (i < minList.size()) {
                mergedList.add(minList.get(i));
            }
            if (i < maxList.size()) {
                mergedList.add(maxList.get(i));
            }
            if (i < sumList.size()) {
                mergedList.add(sumList.get(i));
            }
        }
        return mergedList;
    }

    /**
     * Retrieves tournament data for visualization.
     * For tournament-type disciplines: returns groups with standings and bracket matches.
     * For regular disciplines: returns simple standings based on scores.
     * 
     * @param year Competition year
     * @param category Competition category
     * @param disciplineId Discipline ID
     * @return Tournament visualization data
     */
    public Map<String, Object> getTournamentData(int year, ECategory category, long disciplineId) throws Exception {
        // Verify competition year exists
        if (!this.competitionRepository.findByYear(year).isPresent()) {
            throw new Exception(String.format("failure, competition [%d] not exists", year));
        }

        // Find discipline
        Optional<Discipline> disciplineOpt = this.disciplineRepository.findById(disciplineId);
        if (!disciplineOpt.isPresent()) {
            throw new Exception(String.format("failure, discipline with ID [%d] not exists", disciplineId));
        }
        Discipline discipline = disciplineOpt.get();

        Map<String, Object> result = new HashMap<>();
        result.put("disciplineId", disciplineId);
        result.put("disciplineName", discipline.getName());
        result.put("category", category);
        result.put("year", year);
        result.put("highScoreWin", discipline.getHighScoreWin());

        // Get all matches for this discipline in the given year
        List<RobotMatch> allMatches = robotMatchRepository.findAll().stream()
            .filter(m -> m.getPlayground() != null && 
                        m.getPlayground().getDiscipline().getID() == disciplineId)
            .collect(Collectors.toList());

        // Get short codes for matching group IDs
        String discShort = getDisciplineShortName(discipline);
        String catShort = category == ECategory.LOW_AGE_CATEGORY ? "L" : "H";
        String groupPattern = String.format("%s_%s_%d_", discShort, catShort, year);

        // Filter by category (check either robot or group pattern for bracket matches)
        List<RobotMatch> categoryMatches = allMatches.stream()
            .filter(m -> {
                if (m.getRobotA() != null && m.getRobotA().getCategory() == category && 
                    m.getRobotA().getTeamRegistration().getCompetitionYear() == year) {
                    return true;
                }
                if (m.getRobotB() != null && m.getRobotB().getCategory() == category &&
                    m.getRobotB().getTeamRegistration().getCompetitionYear() == year) {
                    return true;
                }
                // For bracket matches without robots assigned yet, check group pattern
                if (m.getGroup() != null && m.getGroup().startsWith(groupPattern)) {
                    return true;
                }
                return false;
            })
            .collect(Collectors.toList());

        // Check if this is a tournament (has group/bracket structure)
        boolean isTournament = categoryMatches.stream()
            .anyMatch(m -> m.getGroup() != null && !m.getGroup().isEmpty());

        result.put("isTournament", isTournament);

        if (isTournament) {
            // Tournament mode - separate into groups and bracket
            // Use the same format as TournamentGeneratorService: DISC_C_YEAR_X (e.g., ROBOS_H_2026_A)
            String disciplineShortName = getDisciplineShortName(discipline);
            String categoryShortCode = category == ECategory.LOW_AGE_CATEGORY ? "L" : "H";
            String groupPrefix = String.format("%s_%s_%d_", disciplineShortName, categoryShortCode, year);
            String bracketId = String.format("%s_%s_%d_BR", disciplineShortName, categoryShortCode, year);

            // Group matches (group ID ends with A, B, C, etc. - not BR)
            List<RobotMatch> groupMatches = categoryMatches.stream()
                .filter(m -> m.getGroup() != null && m.getGroup().startsWith(groupPrefix) && !m.getGroup().endsWith("_BR"))
                .collect(Collectors.toList());

            // Bracket matches
            List<RobotMatch> bracketMatches = categoryMatches.stream()
                .filter(m -> m.getGroup() != null && m.getGroup().equals(bracketId))
                .collect(Collectors.toList());

            // Process groups
            Map<String, List<RobotMatch>> matchesByGroup = groupMatches.stream()
                .collect(Collectors.groupingBy(RobotMatch::getGroup));

            List<Map<String, Object>> groups = new ArrayList<>();
            Boolean highScoreWin = discipline.getHighScoreWin();

            for (Map.Entry<String, List<RobotMatch>> entry : matchesByGroup.entrySet()) {
                String groupId = entry.getKey();
                List<RobotMatch> gMatches = entry.getValue();
                String groupName = groupId.substring(groupId.lastIndexOf('_') + 1);

                // Calculate standings
                List<Map<String, Object>> standings = calculateGroupStandings(gMatches, highScoreWin);

                // Convert matches to info DTOs
                List<RobotMatchInfo> matchInfos = gMatches.stream()
                    .sorted((a, b) -> {
                        int xComp = Integer.compare(
                            a.getVisualX() != null ? a.getVisualX() : 0,
                            b.getVisualX() != null ? b.getVisualX() : 0);
                        if (xComp != 0) return xComp;
                        return Integer.compare(
                            a.getVisualY() != null ? a.getVisualY() : 0,
                            b.getVisualY() != null ? b.getVisualY() : 0);
                    })
                    .map(RobotMatchInfo::new)
                    .collect(Collectors.toList());

                Map<String, Object> groupInfo = new HashMap<>();
                groupInfo.put("groupId", groupId);
                groupInfo.put("groupName", groupName);
                groupInfo.put("matches", matchInfos);
                groupInfo.put("standings", standings);
                groupInfo.put("totalMatches", gMatches.size());
                groupInfo.put("completedMatches", gMatches.stream()
                    .filter(m -> m.getState().getName() == EMatchState.DONE).count());
                groups.add(groupInfo);
            }

            // Sort groups by name
            groups.sort((a, b) -> ((String) a.get("groupName")).compareTo((String) b.get("groupName")));
            result.put("groups", groups);

            // Process bracket - organize by rounds
            List<Map<String, Object>> bracketRounds = processBracketRounds(bracketMatches);
            result.put("bracket", bracketRounds);
            result.put("totalBracketMatches", bracketMatches.size());
            result.put("completedBracketMatches", bracketMatches.stream()
                .filter(m -> m.getState().getName() == EMatchState.DONE).count());

        } else {
            // Regular discipline mode - simple standings
            List<OrderObj> order = getOrder(year, category, disciplineId);
            result.put("standings", order);
        }

        return result;
    }

    /**
     * Calculate standings for a group based on matches
     */
    private List<Map<String, Object>> calculateGroupStandings(List<RobotMatch> matches, Boolean highScoreWin) {
        Map<Long, Map<String, Object>> robotStats = new HashMap<>();

        for (RobotMatch match : matches) {
            if (match.getState().getName() != EMatchState.DONE) continue;

            Robot robotA = match.getRobotA();
            Robot robotB = match.getRobotB();
            Float scoreA = match.getScoreA();
            Float scoreB = match.getScoreB();

            if (robotA == null || robotB == null || scoreA == null || scoreB == null) continue;

            // Initialize robot stats if needed
            if (!robotStats.containsKey(robotA.getID())) {
                Map<String, Object> stats = new HashMap<>();
                stats.put("robotId", robotA.getID());
                stats.put("robotName", robotA.getName());
                stats.put("robotNumber", robotA.getNumber());
                stats.put("teamName", robotA.getTeamRegistration().getTeam().getName());
                stats.put("wins", 0);
                stats.put("losses", 0);
                stats.put("scoreDiff", 0f);
                robotStats.put(robotA.getID(), stats);
            }
            if (!robotStats.containsKey(robotB.getID())) {
                Map<String, Object> stats = new HashMap<>();
                stats.put("robotId", robotB.getID());
                stats.put("robotName", robotB.getName());
                stats.put("robotNumber", robotB.getNumber());
                stats.put("teamName", robotB.getTeamRegistration().getTeam().getName());
                stats.put("wins", 0);
                stats.put("losses", 0);
                stats.put("scoreDiff", 0f);
                robotStats.put(robotB.getID(), stats);
            }

            // Determine winner
            boolean aWins;
            if (highScoreWin != null && highScoreWin) {
                aWins = scoreA > scoreB;
            } else {
                aWins = scoreA < scoreB;
            }

            Map<String, Object> statsA = robotStats.get(robotA.getID());
            Map<String, Object> statsB = robotStats.get(robotB.getID());

            if (aWins) {
                statsA.put("wins", (int) statsA.get("wins") + 1);
                statsB.put("losses", (int) statsB.get("losses") + 1);
            } else if (!scoreA.equals(scoreB)) {
                statsB.put("wins", (int) statsB.get("wins") + 1);
                statsA.put("losses", (int) statsA.get("losses") + 1);
            }

            statsA.put("scoreDiff", (float) statsA.get("scoreDiff") + (scoreA - scoreB));
            statsB.put("scoreDiff", (float) statsB.get("scoreDiff") + (scoreB - scoreA));
        }

        // Sort by wins, then by scoreDiff
        List<Map<String, Object>> standings = new ArrayList<>(robotStats.values());
        standings.sort((a, b) -> {
            int winsComp = Integer.compare((int) b.get("wins"), (int) a.get("wins"));
            if (winsComp != 0) return winsComp;
            return Float.compare((float) b.get("scoreDiff"), (float) a.get("scoreDiff"));
        });

        // Add rank
        int rank = 1;
        for (Map<String, Object> s : standings) {
            s.put("rank", rank++);
        }

        return standings;
    }

    /**
     * Process bracket matches into rounds for visualization
     */
    private List<Map<String, Object>> processBracketRounds(List<RobotMatch> bracketMatches) {
        // First, separate THIRD_PLACE matches from regular bracket matches
        List<RobotMatch> thirdPlaceMatches = bracketMatches.stream()
            .filter(m -> m.getPhase() != null && m.getPhase().getName() == ETournamentPhase.THIRD_PLACE)
            .collect(Collectors.toList());
        
        List<RobotMatch> regularMatches = bracketMatches.stream()
            .filter(m -> m.getPhase() == null || m.getPhase().getName() != ETournamentPhase.THIRD_PLACE)
            .collect(Collectors.toList());

        // Group regular matches by visualX (round number)
        Map<Integer, List<RobotMatch>> matchesByRound = regularMatches.stream()
            .filter(m -> m.getVisualX() != null)
            .collect(Collectors.groupingBy(RobotMatch::getVisualX));

        List<Map<String, Object>> rounds = new ArrayList<>();

        for (Map.Entry<Integer, List<RobotMatch>> entry : matchesByRound.entrySet()) {
            int roundNumber = entry.getKey();
            List<RobotMatch> roundMatches = entry.getValue();

            // Sort by visualY
            roundMatches.sort((a, b) -> Integer.compare(
                a.getVisualY() != null ? a.getVisualY() : 0,
                b.getVisualY() != null ? b.getVisualY() : 0));

            // Determine round name from first match's phase
            String roundName = "Round " + (roundNumber + 1);
            if (!roundMatches.isEmpty() && roundMatches.get(0).getPhase() != null) {
                ETournamentPhase phase = roundMatches.get(0).getPhase().getName();
                switch (phase) {
                    case FINAL: roundName = "Finále"; break;
                    case SEMIFINAL: roundName = "Semifinále"; break;
                    case QUARTERFINAL: roundName = "Čtvrtfinále"; break;
                    case ROUND_OF_16: roundName = "Osmifinále"; break;
                    case PRELIMINARY: roundName = "Kolo " + (roundNumber + 1); break;
                    case GROUP_STAGE: roundName = "Skupina"; break;
                    default: break;
                }
            }

            List<RobotMatchInfo> matchInfos = roundMatches.stream()
                .map(RobotMatchInfo::new)
                .collect(Collectors.toList());

            Map<String, Object> round = new HashMap<>();
            round.put("roundNumber", roundNumber);
            round.put("name", roundName);
            round.put("matches", matchInfos);
            rounds.add(round);
        }

        // Sort by round number
        rounds.sort((a, b) -> Integer.compare(
            (int) a.get("roundNumber"), (int) b.get("roundNumber")));

        // Add 3rd place match as a separate round at the end (if exists)
        if (!thirdPlaceMatches.isEmpty()) {
            List<RobotMatchInfo> thirdPlaceInfos = thirdPlaceMatches.stream()
                .map(RobotMatchInfo::new)
                .collect(Collectors.toList());
            
            Map<String, Object> thirdPlaceRound = new HashMap<>();
            thirdPlaceRound.put("roundNumber", 9999); // High number to ensure it's last
            thirdPlaceRound.put("name", "O 3. místo");
            thirdPlaceRound.put("matches", thirdPlaceInfos);
            rounds.add(thirdPlaceRound);
        }

        return rounds;
    }

    /**
     * Get short name for discipline (for group ID matching)
     * Must match TournamentGeneratorService format
     */
    private String getDisciplineShortName(Discipline discipline) {
        String name = discipline.getName().toUpperCase()
            .replaceAll("\\s+", "_")
            .replaceAll("[^A-Z0-9_]", "");
        // Return max 6 characters for shorter group IDs
        return name.length() > 6 ? name.substring(0, 6) : name;
    }

    /**
     * Get all winners (1st, 2nd, 3rd place) for all disciplines and categories in a given year.
     * Returns a structured list of disciplines with their winners for each category.
     * 
     * @param year Competition year
     * @return List of discipline winners with robot info
     */
    public List<Map<String, Object>> getAllWinners(int year) throws Exception {
        // Verify that competition year exists
        Optional<Competition> competition = this.competitionRepository.findByYear(year);
        if (!competition.isPresent()) {
            throw new Exception(String.format("failure, competition [%d] not exists", year));
        }

        List<Map<String, Object>> result = new ArrayList<>();

        // Get all disciplines
        List<Discipline> disciplines = this.disciplineRepository.findAll();
        
        for (Discipline discipline : disciplines) {
            Map<String, Object> disciplineData = new HashMap<>();
            disciplineData.put("disciplineId", discipline.getID());
            disciplineData.put("disciplineName", discipline.getName());
            
            List<Map<String, Object>> categoryResults = new ArrayList<>();
            
            // Process both categories
            for (ECategory category : ECategory.values()) {
                try {
                    List<OrderObj> order = getOrder(year, category, discipline.getID());
                    
                    // Get top 3 winners (or less if not enough participants)
                    List<Map<String, Object>> winners = new ArrayList<>();
                    for (int i = 0; i < Math.min(3, order.size()); i++) {
                        OrderObj winner = order.get(i);
                        RobotScore robotScore = winner.getData();
                        Robot robot = robotScore.getRobot();
                        
                        Map<String, Object> winnerData = new HashMap<>();
                        winnerData.put("place", winner.getPlace());
                        winnerData.put("robotId", robot.getID());
                        winnerData.put("robotName", robot.getName());
                        winnerData.put("robotNumber", robot.getNumber());
                        winnerData.put("teamName", robot.getTeamRegistration().getTeam().getName());
                        winnerData.put("score", robotScore.getScore());
                        winners.add(winnerData);
                    }
                    
                    Map<String, Object> catResult = new HashMap<>();
                    catResult.put("category", category.name());
                    catResult.put("categoryLabel", category == ECategory.LOW_AGE_CATEGORY ? "Žáci" : "Studenti a dospělí");
                    catResult.put("winners", winners);
                    catResult.put("hasWinners", !winners.isEmpty());
                    categoryResults.add(catResult);
                } catch (Exception e) {
                    // No participants in this category - add empty result
                    Map<String, Object> catResult = new HashMap<>();
                    catResult.put("category", category.name());
                    catResult.put("categoryLabel", category == ECategory.LOW_AGE_CATEGORY ? "Žáci" : "Studenti a dospělí");
                    catResult.put("winners", new ArrayList<>());
                    catResult.put("hasWinners", false);
                    categoryResults.add(catResult);
                }
            }
            
            disciplineData.put("categories", categoryResults);
            
            // Check if discipline has any winners at all
            boolean hasAnyWinners = categoryResults.stream()
                .anyMatch(c -> (boolean) c.get("hasWinners"));
            disciplineData.put("hasAnyWinners", hasAnyWinners);
            
            result.add(disciplineData);
        }

        return result;
    }

}
