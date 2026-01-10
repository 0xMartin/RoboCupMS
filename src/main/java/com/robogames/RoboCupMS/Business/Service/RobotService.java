package com.robogames.RoboCupMS.Business.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Business.Enum.ECategory;
import com.robogames.RoboCupMS.Business.Object.RobotObj;
import com.robogames.RoboCupMS.Business.Object.RobotProfile;
import com.robogames.RoboCupMS.Entity.Discipline;
import com.robogames.RoboCupMS.Entity.Robot;
import com.robogames.RoboCupMS.Entity.Team;
import com.robogames.RoboCupMS.Entity.TeamRegistration;
import com.robogames.RoboCupMS.Entity.UserRC;
import com.robogames.RoboCupMS.Repository.DisciplineRepository;
import com.robogames.RoboCupMS.Repository.RobotMatchRepository;
import com.robogames.RoboCupMS.Repository.RobotRepository;
import com.robogames.RoboCupMS.Repository.TeamRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Zajistuje spravu souteznich robotu
 */
@Service
public class RobotService {

    @Autowired
    private RobotRepository robotRepository;

    @Autowired
    private RobotMatchRepository robotMatchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private DisciplineRepository disciplineRepository;

    /**
     * Navrati robota s konkretnim ID
     * 
     * @param id ID robota
     * @return Robot
     */
    public Robot get(Long id) throws Exception {
        Optional<Robot> robot = this.robotRepository.findById(id);
        if (robot.isPresent()) {
            return robot.get();
        } else {
            throw new Exception(String.format("failure, robot with ID [%d] not exists", id));
        }
    }

    /**
     * Navrati podrobny profil robota podle id
     * 
     * @param id ID robota
     * @return Profil robota (podrobnejsni info)
     */
    public RobotProfile getRobotProfile(Long id) throws Exception {
        // Ziska robota podle ID
        Optional<Robot> robotOpt = this.robotRepository.findById(id);
        if (!robotOpt.isPresent()) {
            throw new Exception(String.format("failure, robot with ID [%d] not exists", id));
        }
        Robot robot = robotOpt.get();

        // Ziska registraci tymu
        TeamRegistration teamRegistration = robot.getTeamRegistration();
        if (teamRegistration == null) {
            throw new Exception(String.format("failure, robot with ID [%d] has no team registration", id));
        }

        // Ziska tym
        Team team = teamRegistration.getTeam();
        if (team == null) {
            throw new Exception(String.format("failure, team registration has no team", id));
        }

        // Ziska informace o vedoucim tymu
        Long leaderId = team.getLeaderID();
        String leaderName = team.getLeaderName();
        String leaderSurname = team.getLeaderSurname();

        // Ziska jmeno robota
        String robotName = robot.getName();

        // Ziska cislo robota
        Long robotNumber = robot.getNumber();

        // Ziska disciplinu (muze byt null pokud robot neni registrovan)
        String discipline = robot.getDiscipline() != null ? robot.getDiscipline().getName() : "";

        // Ziska kategorii
        ECategory category = robot.getCategory();

        // Ziska nazev tymu
        String teamName = team.getName();

        // Ziska ID tymu
        Long teamId = team.getID();

        // Ziska seznam clenu tymu
        List<RobotProfile.TeamMemberInfo> teamMembers = new ArrayList<>();
        for (UserRC member : team.getMembers()) {
            teamMembers.add(new RobotProfile.TeamMemberInfo(
                    member.getName(),
                    member.getSurname(),
                    member.getEmail()));
        }

        // Ziska pocet clenu tymu
        int teamMemberCount = team.getMemberCount();

        // Ziska informace o uciteli z registrace tymu
        String teacherName = teamRegistration.getTeacherName();
        String teacherSurname = teamRegistration.getTeacherSurname();
        String teacherContact = teamRegistration.getTeacherContact();

        // Vytvori a navrati profil robota
        return new RobotProfile(
                robotName,
                robotNumber,
                discipline,
                category,
                teamName,
                teamId,
                leaderId,
                leaderName,
                leaderSurname,
                teamMembers,
                teamMemberCount,
                teacherName,
                teacherSurname,
                teacherContact,
                robot.getConfirmed());
    }

    /**
     * Navrati vsechny vytvorene robot pro urcitou registraci tymu
     * 
     * @param year Rocnik souteze
     * @return Seznam vsech robotu
     */
    public List<Robot> getAll(int year) throws Exception {
        UserRC user = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // id tymu, ve kterem se uzivatel nachazi
        long team_id = user.getTeamID();
        if (team_id == Team.NOT_IN_TEAM) {
            throw new Exception("failure, you are not a member of any team");
        }

        // najde tym v datavazi
        Optional<Team> t = this.teamRepository.findById(team_id);
        if (!t.isPresent()) {
            throw new Exception("failure, team not exists");
        }

        // najde registraci tymu pro dany rocnik souteze
        List<TeamRegistration> registrations = t.get().getRegistrations();
        Optional<TeamRegistration> registration = registrations.stream().filter(r -> (r.getCompetitionYear() == year))
                .findFirst();
        if (!registration.isPresent()) {
            throw new Exception(String.format("failure, team registration not exists for year [%d]", year));
        }

        // navrati seznam vsech robotu pro danou registraci tymu
        return registration.get().getRobots();
    }

    /**
     * Navrati vsehcny roboty s potvrzenou registraci
     * 
     * @param year Rocnik souteze
     * @return Seznam robotu s potvrzenou registraci
     */
    public List<Robot> getAllConfirmed(int year) throws Exception {
        Stream<Robot> robots = this.robotRepository.findAll().stream()
                .filter((r) -> (r.getConfirmed() && r.getTeamRegistration().getCompetitionYear() == year));

        return robots.collect(Collectors.toList());
    }

    public List<Robot> allForYear(int year) throws Exception {
        Stream<Robot> robots = this.robotRepository.findAll().stream()
                .filter((r) -> (r.getTeamRegistration().getCompetitionYear() == year));
        return robots.collect(Collectors.toList());
    }

    /**
     * Overi delku nazvu robota
     * 
     * @param name Jmeno robota
     * @throws Exception
     */
    private void validateRobotName(String name) throws Exception {
        if (name.length() < GlobalConfig.MIN_ROBOT_NAME_LENGTH) {
            throw new Exception("failure, name is too short");
        } else if (name.length() > GlobalConfig.MAX_ROBOT_NAME_LENGTH) {
            throw new Exception("failure, name is too long");
        }
    }

    /**
     * Vytvori noveho robota. Robot je vytvaren na registraci tymu v urcitem
     * rocniku souteze.
     * 
     * @param year     Rocnik souteze
     * @param robotObj Parametry noveho robota
     */
    public void create(int year, RobotObj robotObj) throws Exception {
        // ziska registraci tymu v danem rocniku souteze pro prihlaseneho uzivatele
        TeamRegistration registration;
        try {
            registration = this.getTeamRegistration(year);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        String name = robotObj.getName();

        // overi delku nazvu robota
        validateRobotName(name);

        // overeni unikatnosti jmena robota v ramci rocniku souteze
        //if (this.robotRepository.findByName(name).isPresent()) {
        //    throw new Exception(
        //            String.format("failure, robot with name [%s] already exists in the year [%d]", name, year));
        //}

        // ulozi robota do databaze
        Robot r = new Robot(robotObj.getName(), 0, registration);
        this.robotRepository.save(r);
    }

    /**
     * Odstrani robota
     * 
     * @param year Rocnik souteze
     * @param id   ID robota
     */
    public void remove(int year, Long id) throws Exception {
        // ziska registraci tymu v danem rocniku souteze pro prihlaseneho uzivatele
        TeamRegistration registration;
        try {
            registration = this.getTeamRegistration(year);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        // overi zda robot existuje v dane registraci tymu
        Optional<Robot> robot = registration.getRobots().stream().filter((r) -> (r.getID() == id)).findFirst();
        if (!robot.isPresent()) {
            throw new Exception(String.format("failure, robot with ID [%d] not found", id));
        }

        // pokud registrace robota byla jiz potvrzena tak neumozni jeho odstraneni
        if (robot.get().getConfirmed()) {
            throw new Exception(String.format("failure, robot with ID [%d] has already been confirmed", id));
        }

        if (!robot.get().getMatches().isEmpty()) {
            this.robotMatchRepository.deleteAll(robot.get().getMatches());
        }

        // odstrani robota
        this.robotRepository.delete(robot.get());
    }

    /**
     * Zmeni jmeno robota
     * 
     * @param year Rocnik souteze
     * @param id   ID robota
     * @param name Nove jmeno robota
     */
    public void rename(int year, Long id, String name) throws Exception {
        // ziska registraci tymu v danem rocniku souteze pro prihlaseneho uzivatele
        TeamRegistration registration;
        try {
            registration = this.getTeamRegistration(year);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        // overi delku nazvu robota
        validateRobotName(name);

        // overeni unikatnosti jmena robota v ramci rocniku souteze
        if (this.robotRepository.findByName(name).isPresent()) {
            throw new Exception(
                    String.format("failure, robot with name [%s] already exists in the year [%d]", name, year));
        }

        Optional<Robot> robot = registration.getRobots().stream().filter((r) -> (r.getID()) == id).findFirst();
        // robot nenalezen
        if (!robot.isPresent()) {
            throw new Exception(String.format("failure, robot with ID [%d] not found", id));
        }

        // overi zda registrace robota jiz nebyla potvrzena
        if (robot.get().getConfirmed()) {
            throw new Exception(String.format("failure, robot with ID [%d] has already been confirmed", id));
        }

        // prejmenuje robota
        robot.get().setName(name);
        this.robotRepository.save(robot.get());
    }

    /**
     * Registuje existujiciho robota do vybrane discipliny
     * 
     * @param robotID      ID robota, ktereho registrujeme
     * @param disciplineID ID discipliny, do ktere chceme robota registrovat
     */
    public void register(Long robotID, Long disciplineID) throws Exception {
        // overi zda robot existuje
        Optional<Robot> robot = this.robotRepository.findById(robotID);
        if (!robot.isPresent()) {
            throw new Exception(String.format("failure, robot with ID [%d] not found", robotID));
        }

        // overi zda robot jiz neni registrovany
        if (robot.get().getDiscipline() != null) {
            throw new Exception(String.format("failure, robot with ID [%d] is already registered", robotID));
        }

        // overi zda registrace robota jiz nebyla povrzena
        if (robot.get().getConfirmed()) {
            throw new Exception(String.format("failure, robot with ID [%d] has already been confirmed", robotID));
        }

        // overi zda discipliny existuje
        Optional<Discipline> discipline = this.disciplineRepository.findById(disciplineID);
        if (!discipline.isPresent()) {
            throw new Exception(String.format("failure, discipline with ID [%d] not exists", disciplineID));
        }

        // overi zda jiz nebyl prekrocen limit na maximalni pocet registrovanych robotu
        // v jedne discipline pro tym
        List<Robot> robots = discipline.get().getRobots();
        long regID = robot.get().getTeamRegistrationID();
        int cnt = 0;
        for (Robot r : robots) {
            if (r.getTeamRegistrationID() == regID) {
                cnt++;
            }
        }
        if (cnt >= GlobalConfig.MAX_ROBOTS_IN_DISCIPLINE) {
            throw new Exception("failure, you have exceeded the maximum limit of registered robots per discipline");
        }

        // zjisti zda uzivatel vlastni tohoto robota (je v tymu, ktery ho vlastni)
        UserRC user = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean ownership = robot.get().getTeamRegistration().getTeam().getMembers().stream()
                .anyMatch((m) -> (m.getID() == user.getID()));

        // provede zmeni (pokud uzivatel robota vlastni pak ho registruje do kategorie)
        if (ownership) {
            robot.get().setDicipline(discipline.get());
            this.robotRepository.save(robot.get());
        } else {
            throw new Exception(String.format("failure, you don't own a robot with ID [%d]", robotID));
        }
    }

    /**
     * Zrusi registraci existujiciho robota
     * 
     * @param id ID robota
     */
    public void unregister(Long id) throws Exception {
        // overi zda robot existuje
        Optional<Robot> robot = this.robotRepository.findById(id);
        if (!robot.isPresent()) {
            throw new Exception(String.format("failure, robot with ID [%d] not found", id));
        }

        // overi zda robot je robot rigistrovany v nejake discipline
        if (robot.get().getDiscipline() == null) {
            throw new Exception(String.format("failure, robot with ID [%d] is not registered", id));
        }

        // overi zda registrace robota jiz nebyla povrzena
        if (robot.get().getConfirmed()) {
            throw new Exception(String.format("failure, robot with ID [%d] has already been confirmed", id));
        }

        // zjisti zda uzivatel vlastni tohoto robota (je v tymu, ktery ho vlastni)
        UserRC user = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean ownership = robot.get().getTeamRegistration().getTeam().getMembers().stream()
                .anyMatch((m) -> (m.getID() == user.getID()));

        // provede zmeni (pokud uzivatel robota vlastni pak ho registruje do kategorie)
        if (ownership) {
            robot.get().setDicipline(null);
            this.robotRepository.save(robot.get());
        } else {
            throw new Exception(String.format("failure, you don't own a robot with ID [%d]", id));
        }
    }

    /**
     * Povrdi nebo nepovrdi registraci robota
     * 
     * @param id        ID robota
     * @param confirmed Registrace je nebo neni povrzena
     */
    public void confirmRegistration(Long id, Boolean confirmed) throws Exception {
        // overi zda robot existuje
        Optional<Robot> robot = this.robotRepository.findById(id);
        if (!robot.isPresent()) {
            throw new Exception(String.format("failure, robot with ID [%d] not found", id));
        }

        // overi zda je robot prihlasen v nejake discipline
        if (robot.get().getDiscipline() == null) {
            throw new Exception(String.format("failure, robot with ID [%d] is not registered", id));
        }

        // provede zmeny
        robot.get().setConfirmed(confirmed);

        // jen pokud byla registrace povrzena
        if (confirmed) {
            // vygeneruje unikatni identifikacni cislo pro robota v ramci rocniku souteze a
            // kategorie
            long max = 0;

            List<Robot> robots = robot.get().getDiscipline().getRobots();
            for (Robot r : robots) {
                max = Math.max(max, r.getNumber());
            }

            // zapise nove cislo (o jedno vetsi nez maximalni hodnota)
            robot.get().setNumber(max + 1);
        }

        this.robotRepository.save(robot.get());
    }

    /**
     * Lokalni metoda pro nelezeni registrace tymu pro daneho prihlaseneho
     * uzivatele, na kterou je mozne vytvaret roboty.
     * 
     * @param year Rocnik souteze
     * @return TeamRegistration
     * @throws Exception
     */
    private TeamRegistration getTeamRegistration(int year) throws Exception {
        UserRC user = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // id tymu, ve kterem se uzivatel nachazi
        long team_id = user.getTeamID();
        if (team_id == Team.NOT_IN_TEAM) {
            throw new Exception("failure, you are not a member of any team");
        }

        // najde tym v databazi
        Optional<Team> t = this.teamRepository.findById(team_id);
        if (!t.isPresent()) {
            throw new Exception("failure, team not exists");
        }

        // najde registraci tymu pro dany rocnik souteze
        List<TeamRegistration> registrations = t.get().getRegistrations();
        Optional<TeamRegistration> registration = registrations.stream().filter(r -> (r.getCompetitionYear() == year))
                .findFirst();
        if (!registration.isPresent()) {
            throw new Exception(String.format("failure, team registration not exists for year [%d]", year));
        }

        // robota je mozne registrovat jen pokud soutez jeste nezacala
        if (registration.get().getCompetition().getStarted()) {
            throw new Exception("failure, competition has already begun");
        }

        return registration.get();
    }

}
