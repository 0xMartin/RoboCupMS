package com.robogames.RoboCupMS.Module.CompetitionEvaluation.Bussiness.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.robogames.RoboCupMS.Business.Enum.ECategory;
import com.robogames.RoboCupMS.Business.Enum.EMatchState;
import com.robogames.RoboCupMS.Business.Enum.EScoreAggregation;
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
     * Returns the placement of robots in a specific discipline within a competition category
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

        // find all robots in discipline who played in the given year
        Stream<Robot> robots = discipline.get().getRobots().stream()
                .filter((r) -> (r.getTeamRegistration().getCompetitionYear() == year));

        // score aggregation function
        ScoreAggregation ag = discipline.get().getScoreAggregation();

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
            // MIN -> from lowest score to highest (line follower, drag race, ...
            // => score represents time)
            Collections.sort(all, new Comparator<RobotScore>() {
                @Override
                public int compare(RobotScore r1, RobotScore r2) {
                    return r2.getScore() < r1.getScore() ? 1 : -1;
                }
            });
        } else {
            // MAX or SUM -> from highest to lowest (sumo, robostrong, ..)
            Collections.sort(all, new Comparator<RobotScore>() {
                @Override
                public int compare(RobotScore r1, RobotScore r2) {
                    return r2.getScore() > r1.getScore() ? 1 : -1;
                }
            });
        }

        List<OrderObj> order = new LinkedList<OrderObj>();
        int place = 1;
        for (RobotScore scoreObj : all) {
            order.add(new OrderObj(place++, scoreObj));
        }

        // return winner of discipline in the given competition category
        return order;
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

}
