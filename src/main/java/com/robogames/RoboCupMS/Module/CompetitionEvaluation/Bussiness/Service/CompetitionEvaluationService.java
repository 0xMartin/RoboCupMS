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
     * Navrati skore vsech robotu, kteri soutezili v danem rocniku
     * 
     * @param year     Rocnik souteze
     * @param category Kategorie, pro kterou má zobrazit výsledky
     * @return Seznam vsech robotu a jejich skore v soutezi
     */
    public List<RobotScore> getScoreOfAll(int year, ECategory category) throws Exception {
        // overi zda rocnik souteze existuje
        Optional<Competition> competition = this.competitionRepository.findByYear(year);
        if (!competition.isPresent()) {
            throw new Exception(String.format("failure, competition [%d] not exists", year));
        }

        // pro vsechny registrovane tymy vypoci celkove skore u vsech jejich potvrzenych
        // robotu
        List<RobotScore> scoreList = new LinkedList<RobotScore>();

        List<TeamRegistration> registrations = competition.get().getRegistrations();
        for (TeamRegistration reg : registrations) {
            List<Robot> robots = reg.getRobots();
            for (Robot r : robots) {
                if (!r.getConfirmed() || r.getCategory() != category)
                    continue;

                // agregacni funkce skore
                ScoreAggregation ag = r.getDiscipline().getScoreAggregation();
                // funkci aplikuje pro vsechny zapasy, ktere robot odehral
                float totalScore = ag.getTotalScoreInitValue();
                List<RobotMatch> matches = r.getMatches();
                for (RobotMatch m : matches) {
                    if (m.getState().getName() == EMatchState.DONE) {
                        totalScore = ag.proccess(totalScore, m.getScore());
                    }
                }

                // score s robotem zapise do listu
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
     * Navrati skore vsech robotu urciteho tymu
     * 
     * @param year Rocnik
     * @param id   ID tymu
     * @return Navrati skore vsech reobotu v tymu
     */
    public TeamScore getScoreOfTeam(int year, long id) throws Exception {
        // overi zda tym existuje
        Optional<Team> team = this.teamRepository.findById(id);
        if (!team.isPresent()) {
            throw new Exception(String.format("failure, team with ID [%d] not exists", id));
        }

        // najde registraci pro dany rocnik souteze
        Optional<TeamRegistration> registration = team.get().getRegistrations().stream()
                .filter((r) -> (r.getCompetitionYear() == year)).findFirst();
        if (!registration.isPresent()) {
            throw new Exception(String.format("failure, registration for year [%d] not exists", year));
        }

        // pro kazdeho robota spocita skore
        List<RobotScore> scoreList = new ArrayList<RobotScore>();
        List<Robot> robots = registration.get().getRobots();

        robots.stream().forEach((r) -> {
            // prihlaska robota musi byt potvrzena
            if (r.getConfirmed()) {
                // agregacni funkce skore
                ScoreAggregation ag = r.getDiscipline().getScoreAggregation();
                // funkci aplikuje pro vsechny zapasy, ktere robot odehral
                float totalScore = ag.getTotalScoreInitValue();
                List<RobotMatch> matches = r.getMatches();
                for (RobotMatch m : matches) {
                    if (m.getState().getName() == EMatchState.DONE) {
                        totalScore = ag.proccess(totalScore, m.getScore());
                    }
                }
                scoreList.add(new RobotScore(r, totalScore));
            }
        });

        return new TeamScore(team.get(), scoreList);
    }

    /**
     * Navrati skore jednoho konkreniho robota
     * 
     * @param year Rocnik souteze
     * @param id   ID robota
     * @return Navrati skore robota
     */
    public RobotScore getScoreOfRobot(int year, long id) throws Exception {
        Optional<Robot> robot = this.robotRepository.findById(id);
        if (!robot.isPresent()) {
            throw new Exception(String.format("failure, robot with ID [%d] not exists", id));
        }

        // overi povrzeni robota
        if (!robot.get().getConfirmed()) {
            throw new Exception(String.format("failure, robot with ID [%d] is not confirmed", id));
        }

        // overi rocnik souteze
        if (robot.get().getTeamRegistration().getCompetitionYear() != year) {
            throw new Exception(String.format("failure, this robot is not registed in year [%d]", year));
        }

        // agregacni funkce skore
        ScoreAggregation ag = robot.get().getDiscipline().getScoreAggregation();
        // funkci aplikuje pro vsechny zapasy, ktere robot odehral
        float totalScore = ag.getTotalScoreInitValue();
        List<RobotMatch> matches = robot.get().getMatches();
        for (RobotMatch m : matches) {
            if (m.getState().getName() == EMatchState.DONE) {
                totalScore = ag.proccess(totalScore, m.getScore());
            }
        }

        return new RobotScore(robot.get(), totalScore);
    }

    /**
     * Navrati umisteni robotu v konkretni discipline v ramci soutezni kategorie
     * 
     * @param year     Rocnik souteze
     * @param category Soutezni kategorie
     * @param id       ID discipliny
     * @return Poradi vsech robotu, kteri soutezili v dane discipline + kategorii
     */
    public List<OrderObj> getOrder(int year, ECategory category, long id) throws Exception {
        // overi zda rocnik souteze existuje
        if (!this.competitionRepository.findByYear(year).isPresent()) {
            throw new Exception(String.format("failure, competition [%d] not exists", year));
        }

        // najde discipliny
        Optional<Discipline> discipline = this.disciplineRepository.findById(id);
        if (!discipline.isPresent()) {
            throw new Exception(String.format("failure, discipline with ID [%d] not exists", id));
        }

        // najde vsechny roboty v discipline, kteri hrali v danem roce
        Stream<Robot> robots = discipline.get().getRobots().stream()
                .filter((r) -> (r.getTeamRegistration().getCompetitionYear() == year));

        // agregacni funkce skore
        ScoreAggregation ag = discipline.get().getScoreAggregation();

        List<RobotScore> all = new LinkedList<>();
        robots.forEach(r -> {
            if (r.getConfirmed() && r.getCategory() == category) {
                // funkci aplikuje pro vsechny zapasy, ktere robot odehral
                float totalScore = ag.getTotalScoreInitValue();
                List<RobotMatch> matches = r.getMatches();
                for (RobotMatch m : matches) {
                    if (m.getState().getName() == EMatchState.DONE) {
                        totalScore = ag.proccess(totalScore, m.getScore());
                    }
                }

                // score s robotem zapise do listu
                all.add(new RobotScore(r, totalScore));
            }
        });

        // serazeni
        if (ag.getName() == EScoreAggregation.MIN) {
            // MIN -> od nejnizsi hodnoty skore po nejvestisi (line follower, drag race, ...
            // => score reprezenutje cas)
            Collections.sort(all, new Comparator<RobotScore>() {
                @Override
                public int compare(RobotScore r1, RobotScore r2) {
                    return r2.getScore() < r1.getScore() ? 1 : -1;
                }
            });
        } else {
            // MAX nebo SUM -> od nejvyssi po nejnizsi (sumo, robostrong, ..)
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

        // navrati viteze discipliny v dane soutezni kategorii
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
        // serazeni
        if (aggregation == EScoreAggregation.MIN) {
            // MIN -> od nejnizsi hodnoty skore po nejvestisi (line follower, drag race, ...
            // => score reprezenutje cas)
            Collections.sort(scoreList, new Comparator<RobotScore>() {
                @Override
                public int compare(RobotScore r1, RobotScore r2) {
                    return r2.getScore() < r1.getScore() ? 1 : -1;
                }
            });
        } else {
            // MAX nebo SUM -> od nejvyssi po nejnizsi (sumo, robostrong, ..)
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
