package com.robogames.RoboCupMS.Business.Service;

import java.util.List;
import java.util.Optional;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Business.Enum.ECategory;
import com.robogames.RoboCupMS.Business.Object.AdminRobotCreateObj;
import com.robogames.RoboCupMS.Business.Object.AdminRobotEditObj;
import com.robogames.RoboCupMS.Business.Object.AdminTeacherEditObj;
import com.robogames.RoboCupMS.Business.Object.AdminTeamCreateObj;
import com.robogames.RoboCupMS.Business.Object.AdminTeamEditObj;
import com.robogames.RoboCupMS.Business.Object.AdminTeamRegistrationObj;
import com.robogames.RoboCupMS.Entity.Category;
import com.robogames.RoboCupMS.Entity.Competition;
import com.robogames.RoboCupMS.Entity.Discipline;
import com.robogames.RoboCupMS.Entity.Robot;
import com.robogames.RoboCupMS.Entity.Team;
import com.robogames.RoboCupMS.Entity.TeamRegistration;
import com.robogames.RoboCupMS.Entity.UserRC;
import com.robogames.RoboCupMS.Repository.CategoryRepository;
import com.robogames.RoboCupMS.Repository.CompetitionRepository;
import com.robogames.RoboCupMS.Repository.DisciplineRepository;
import com.robogames.RoboCupMS.Repository.RobotMatchRepository;
import com.robogames.RoboCupMS.Repository.RobotRepository;
import com.robogames.RoboCupMS.Repository.TeamRegistrationRepository;
import com.robogames.RoboCupMS.Repository.TeamRepository;
import com.robogames.RoboCupMS.Repository.UserRepository;
import com.robogames.RoboCupMS.Module.OrderManagement.Bussiness.Service.OrderManagementService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sluzba pro administrativni operace - umoznuje adminovi menit data v aplikaci
 * nezavisle na beznych omezenich (napr. po zahajeni souteze).
 */
@Service
public class AdminService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRegistrationRepository teamRegistrationRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RobotRepository robotRepository;

    @Autowired
    private RobotMatchRepository robotMatchRepository;

    @Autowired
    private DisciplineRepository disciplineRepository;

    @Autowired
    private OrderManagementService orderManagementService;

    // ==================== TEAM OPERATIONS ====================

    /**
     * Overi platnost nazvu tymu
     * 
     * @param name Nazev tymu
     * @throws Exception
     */
    private void validateTeamName(String name) throws Exception {
        if (name == null || name.isEmpty()) {
            throw new Exception("failure, team name is required");
        }
        if (name.length() < GlobalConfig.MIN_TEAM_NAME_LENGTH) {
            throw new Exception("failure, team name is too short");
        }
        if (name.length() > GlobalConfig.MAX_TEAM_NAME_LENGTH) {
            throw new Exception("failure, team name is too long");
        }
    }

    /**
     * Vytvori novy tym (admin muze specifikovat vedouciho a cleny)
     * 
     * @param teamCreateObj Parametry noveho tymu
     * @return Vytvoreny tym
     * @throws Exception
     */
    @Transactional
    public Team createTeam(AdminTeamCreateObj teamCreateObj) throws Exception {
        String name = teamCreateObj.getName();
        Long leaderId = teamCreateObj.getLeaderId();
        List<Long> memberIds = teamCreateObj.getMemberIds();

        // validace nazvu
        validateTeamName(name);

        // overeni unikatnosti nazvu tymu
        if (this.teamRepository.findByName(name).isPresent()) {
            throw new Exception("failure, team with this name already exists");
        }

        // overeni existence vedouciho
        if (leaderId == null) {
            throw new Exception("failure, leader ID is required");
        }
        Optional<UserRC> leaderOpt = this.userRepository.findById(leaderId);
        if (!leaderOpt.isPresent()) {
            throw new Exception(String.format("failure, user with ID [%d] not found", leaderId));
        }
        UserRC leader = leaderOpt.get();

        // pokud je vedouci v jinem tymu, odebere ho z nej
        if (leader.getTeamID() != Team.NOT_IN_TEAM) {
            Optional<Team> oldTeam = this.teamRepository.findById(leader.getTeamID());
            if (oldTeam.isPresent()) {
                Team old = oldTeam.get();
                // pokud byl vedoucim stareho tymu, zrusi vazbu
                if (old.getLeaderID() == leader.getID()) {
                    old.setLeader(null);
                }
                old.removeMember(leader);
                this.teamRepository.save(old);
            }
        }

        // vytvoreni tymu
        Team team = new Team(name, leader);
        this.teamRepository.save(team);
        this.userRepository.save(leader);

        // pridani clenu do tymu
        if (memberIds != null && !memberIds.isEmpty()) {
            for (Long memberId : memberIds) {
                // preskoc vedouciho (uz je clenem)
                if (memberId.equals(leaderId)) {
                    continue;
                }

                Optional<UserRC> memberOpt = this.userRepository.findById(memberId);
                if (!memberOpt.isPresent()) {
                    throw new Exception(String.format("failure, user with ID [%d] not found", memberId));
                }
                UserRC member = memberOpt.get();

                // pokud je clen v jinem tymu, odebere ho z nej
                if (member.getTeamID() != Team.NOT_IN_TEAM) {
                    Optional<Team> oldTeam = this.teamRepository.findById(member.getTeamID());
                    if (oldTeam.isPresent()) {
                        Team old = oldTeam.get();
                        // pokud byl vedoucim stareho tymu, zrusi vazbu
                        if (old.getLeaderID() == member.getID()) {
                            old.setLeader(null);
                        }
                        old.removeMember(member);
                        this.teamRepository.save(old);
                    }
                }

                // prida clena do noveho tymu
                team.addMember(member);
                this.userRepository.save(member);
            }

            // kontrola maximialniho poctu clenu
            if (team.getMembers().size() > GlobalConfig.MAX_TEAM_MEMBERS) {
                throw new Exception(String.format("failure, team can have maximum %d members", GlobalConfig.MAX_TEAM_MEMBERS));
            }
        }

        return this.teamRepository.save(team);
    }

    /**
     * Odstrani tym (s omezenimi pro zachovani integrity dat)
     * Nelze odstranit tym pokud:
     * - Je registrovan v jiz zahajene soutezi
     * - Ma roboty s existujicimi zapasy
     * - Ma potvrzene roboty
     * 
     * @param teamId ID tymu
     * @throws Exception
     */
    @Transactional
    public void removeTeam(Long teamId) throws Exception {
        Optional<Team> teamOpt = this.teamRepository.findById(teamId);
        if (!teamOpt.isPresent()) {
            throw new Exception(String.format("failure, team with ID [%d] not found", teamId));
        }
        Team team = teamOpt.get();

        // kontrola registraci
        for (TeamRegistration reg : team.getRegistrations()) {
            // nesmaze tym pokud je registrovan v jiz zahajene soutezi
            if (Boolean.TRUE.equals(reg.getCompetition().getStarted())) {
                throw new Exception(
                        String.format("failure, cannot remove team because it is registered in started competition [%d]", 
                                reg.getCompetitionYear()));
            }

            for (Robot r : reg.getRobots()) {
                // nesmaze tym pokud ma potvrzene roboty
                if (r.getConfirmed()) {
                    throw new Exception(
                            "failure, cannot remove team because it has confirmed robots (data preservation)");
                }
                // nesmaze tym pokud roboti maji zapasy
                if (!r.getMatches().isEmpty()) {
                    throw new Exception(
                            String.format("failure, cannot remove team because robot [%s] has existing matches", r.getName()));
                }
            }
        }

        // odebere cleny z tymu (nastavi jim team na null) - DULEZITE: pred smazanim tymu!
        for (UserRC member : team.getMembers()) {
            member.setTeam(null);
        }
        this.userRepository.saveAll(team.getMembers());

        // Explicitne odstran roboty a registrace
        for (TeamRegistration reg : team.getRegistrations()) {
            for (Robot r : reg.getRobots()) {
                this.robotRepository.delete(r);
            }
            this.teamRegistrationRepository.delete(reg);
        }

        // odstrani tym
        this.teamRepository.delete(team);
    }

    /**
     * Edituje udaje tymu
     * 
     * @param teamId  ID tymu
     * @param editObj Nove udaje tymu
     * @return Aktualizovany tym
     * @throws Exception
     */
    @Transactional
    public Team editTeam(Long teamId, AdminTeamEditObj editObj) throws Exception {
        Optional<Team> teamOpt = this.teamRepository.findById(teamId);
        if (!teamOpt.isPresent()) {
            throw new Exception(String.format("failure, team with ID [%d] not found", teamId));
        }
        Team team = teamOpt.get();

        // zmena nazvu
        if (editObj.getName() != null) {
            validateTeamName(editObj.getName());

            // overeni unikatnosti nazvu
            Optional<Team> existingTeam = this.teamRepository.findByName(editObj.getName());
            if (existingTeam.isPresent() && !existingTeam.get().getID().equals(teamId)) {
                throw new Exception("failure, team with this name already exists");
            }

            team.setName(editObj.getName());
        }

        // zmena vedouciho
        if (editObj.getLeaderId() != null) {
            Optional<UserRC> newLeaderOpt = this.userRepository.findById(editObj.getLeaderId());
            if (!newLeaderOpt.isPresent()) {
                throw new Exception(String.format("failure, user with ID [%d] not found", editObj.getLeaderId()));
            }
            UserRC newLeader = newLeaderOpt.get();

            // pokud novy vedouci neni clenem tymu, pridame ho
            if (newLeader.getTeamID() != team.getID()) {
                // pokud je v jinem tymu, odebereme ho
                if (newLeader.getTeamID() != Team.NOT_IN_TEAM) {
                    Optional<Team> oldTeam = this.teamRepository.findById(newLeader.getTeamID());
                    if (oldTeam.isPresent()) {
                        Team old = oldTeam.get();
                        // pokud byl vedoucim stareho tymu, zrusi vazbu
                        if (old.getLeaderID() == newLeader.getID()) {
                            old.setLeader(null);
                        }
                        old.removeMember(newLeader);
                        this.teamRepository.save(old);
                    }
                }
                team.addMember(newLeader);
                this.userRepository.save(newLeader);
            }

            team.setLeader(newLeader);
        }

        return this.teamRepository.save(team);
    }

    /**
     * Prida uzivatele do tymu (pokud je v jinem tymu, odebere ho z nej)
     * 
     * @param teamId ID tymu
     * @param userId ID uzivatele
     * @throws Exception
     */
    @Transactional
    public void addUserToTeam(Long teamId, Long userId) throws Exception {
        Optional<Team> teamOpt = this.teamRepository.findById(teamId);
        if (!teamOpt.isPresent()) {
            throw new Exception(String.format("failure, team with ID [%d] not found", teamId));
        }
        Team team = teamOpt.get();

        Optional<UserRC> userOpt = this.userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new Exception(String.format("failure, user with ID [%d] not found", userId));
        }
        UserRC user = userOpt.get();

        // kontrola maximialniho poctu clenu
        if (team.getMembers().size() >= GlobalConfig.MAX_TEAM_MEMBERS) {
            throw new Exception(String.format("failure, team already has maximum %d members", GlobalConfig.MAX_TEAM_MEMBERS));
        }

        // pokud je uz v tomto tymu, nic nedelame
        if (user.getTeamID() == team.getID()) {
            throw new Exception("failure, user is already a member of this team");
        }

        // pokud je v jinem tymu, odebereme ho
        if (user.getTeamID() != Team.NOT_IN_TEAM) {
            Optional<Team> oldTeam = this.teamRepository.findById(user.getTeamID());
            if (oldTeam.isPresent()) {
                Team old = oldTeam.get();
                // pokud byl vedoucim stareho tymu, najdeme noveho vedouciho
                if (old.getLeaderID() == user.getID()) {
                    // najdeme jineho clena tymu
                    Optional<UserRC> newLeader = old.getMembers().stream()
                            .filter(m -> m.getID() != user.getID())
                            .findFirst();
                    
                    if (newLeader.isPresent()) {
                        old.setLeader(newLeader.get());
                    } else {
                        old.setLeader(null);
                    }
                }
                old.removeMember(user);
                this.teamRepository.save(old);
            }
        }

        // pridame do noveho tymu (Team.addMember automaticky nastavi vedouciho pokud tym zadneho nema)
        team.addMember(user);
        this.teamRepository.save(team);
        this.userRepository.save(user);
    }

    /**
     * Odebere uzivatele z tymu
     * 
     * @param teamId ID tymu
     * @param userId ID uzivatele
     * @throws Exception
     */
    @Transactional
    public void removeUserFromTeam(Long teamId, Long userId) throws Exception {
        Optional<Team> teamOpt = this.teamRepository.findById(teamId);
        if (!teamOpt.isPresent()) {
            throw new Exception(String.format("failure, team with ID [%d] not found", teamId));
        }
        Team team = teamOpt.get();

        Optional<UserRC> userOpt = this.userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new Exception(String.format("failure, user with ID [%d] not found", userId));
        }
        UserRC user = userOpt.get();

        // kontrola ze uzivatel je clenem tymu
        if (user.getTeamID() != team.getID()) {
            throw new Exception("failure, user is not a member of this team");
        }

        // pokud je uzivatel vedoucim tymu, predame roli jinemu clenovi
        if (team.getLeaderID() == userId) {
            // najdeme jineho clena tymu
            Optional<UserRC> newLeader = team.getMembers().stream()
                    .filter(m -> m.getID() != userId)
                    .findFirst();

            if (newLeader.isPresent()) {
                team.setLeader(newLeader.get());
            } else {
                // pokud neni zadny jiny clen, nastavime vedouciho na null
                team.setLeader(null);
            }
        }

        // odebereme z tymu
        team.removeMember(user);
        this.teamRepository.save(team);
        this.userRepository.save(user);
    }

    // ==================== TEAM REGISTRATION OPERATIONS ====================

    /**
     * Registruje tym do souteze (admin muze registrovat kdykoliv, i po zahajeni souteze)
     * 
     * @param registrationObj Parametry registrace
     * @return Vytvorena registrace
     * @throws Exception
     */
    @Transactional
    public TeamRegistration registerTeamToCompetition(AdminTeamRegistrationObj registrationObj) throws Exception {
        Long teamId = registrationObj.getTeamId();
        int year = registrationObj.getYear();

        // overeni existence tymu
        Optional<Team> teamOpt = this.teamRepository.findById(teamId);
        if (!teamOpt.isPresent()) {
            throw new Exception(String.format("failure, team with ID [%d] not found", teamId));
        }
        Team team = teamOpt.get();

        // overeni existence rocniku souteze
        Optional<Competition> competitionOpt = this.competitionRepository.findByYear(year);
        if (!competitionOpt.isPresent()) {
            throw new Exception(String.format("failure, competition [%d] not exists", year));
        }
        Competition competition = competitionOpt.get();

        // overeni ze tym jiz neni registrovan v tomto rocniku
        boolean alreadyRegistered = team.getRegistrations().stream()
                .anyMatch(r -> r.getCompetitionYear() == year);
        if (alreadyRegistered) {
            throw new Exception("failure, team is already registered in this year of competition");
        }

        // urceni kategorie tymu
        ECategory catName = team.determinateCategory();
        Optional<Category> categoryOpt = this.categoryRepository.findByName(catName);
        if (!categoryOpt.isPresent()) {
            throw new Exception("failure, category not exists");
        }

        // vytvoreni registrace
        TeamRegistration registration = new TeamRegistration(team, competition, categoryOpt.get());

        // nastaveni udaju o uciteli
        if (registrationObj.getTeacherName() != null) {
            registration.setTeacherName(registrationObj.getTeacherName());
        }
        if (registrationObj.getTeacherSurname() != null) {
            registration.setTeacherSurname(registrationObj.getTeacherSurname());
        }
        if (registrationObj.getTeacherContact() != null) {
            registration.setTeacherContact(registrationObj.getTeacherContact());
        }

        return this.teamRegistrationRepository.save(registration);
    }

    /**
     * Zrusi registraci tymu ze souteze (admin muze zrusit kdykoliv, ale zachova data pokud jsou potvrzeni roboti)
     * 
     * @param registrationId ID registrace tymu
     * @throws Exception
     */
    @Transactional
    public void unregisterTeamFromCompetition(Long registrationId) throws Exception {
        Optional<TeamRegistration> registrationOpt = this.teamRegistrationRepository.findById(registrationId);
        if (!registrationOpt.isPresent()) {
            throw new Exception(String.format("failure, team registration with ID [%d] not found", registrationId));
        }
        TeamRegistration registration = registrationOpt.get();

        // kontrola ze zadny robot neni potvrzeny (zachovani dat)
        for (Robot r : registration.getRobots()) {
            if (r.getConfirmed()) {
                throw new Exception(
                        "failure, cannot remove registration because it has confirmed robots (data preservation)");
            }
        }

        // kaskadove smazani robotu (jsou nepotvrzen) pred smazanim registrace
        for (Robot r : registration.getRobots()) {
            if (!r.getMatches().isEmpty()) {
                this.robotMatchRepository.deleteAll(r.getMatches());
            }
        }

        this.teamRegistrationRepository.delete(registration);
    }

    /**
     * Zmeni udaje o uciteli (zodpovedne osobe) v registraci tymu
     * 
     * @param registrationId ID registrace tymu
     * @param teacherObj     Nove udaje o uciteli
     * @return Aktualizovana registrace
     * @throws Exception
     */
    @Transactional
    public TeamRegistration editTeacherInfo(Long registrationId, AdminTeacherEditObj teacherObj) throws Exception {
        Optional<TeamRegistration> registrationOpt = this.teamRegistrationRepository.findById(registrationId);
        if (!registrationOpt.isPresent()) {
            throw new Exception(String.format("failure, team registration with ID [%d] not found", registrationId));
        }
        TeamRegistration registration = registrationOpt.get();

        if (teacherObj.getTeacherName() != null) {
            registration.setTeacherName(teacherObj.getTeacherName());
        }
        if (teacherObj.getTeacherSurname() != null) {
            registration.setTeacherSurname(teacherObj.getTeacherSurname());
        }
        if (teacherObj.getTeacherContact() != null) {
            registration.setTeacherContact(teacherObj.getTeacherContact());
        }

        return this.teamRegistrationRepository.save(registration);
    }

    // ==================== ROBOT OPERATIONS ====================

    /**
     * Overi platnost jmena robota
     * 
     * @param name Jmeno robota
     * @throws Exception
     */
    private void validateRobotName(String name) throws Exception {
        if (name == null || name.isEmpty()) {
            throw new Exception("failure, robot name is required");
        }
        if (name.length() < GlobalConfig.MIN_ROBOT_NAME_LENGTH) {
            throw new Exception("failure, robot name is too short");
        }
        if (name.length() > GlobalConfig.MAX_ROBOT_NAME_LENGTH) {
            throw new Exception("failure, robot name is too long");
        }
    }

    /**
     * Vytvori robota na existujici registraci tymu (stav jako nepotvrzeny)
     * 
     * @param robotCreateObj Parametry noveho robota
     * @return Vytvoreny robot
     * @throws Exception
     */
    @Transactional
    public Robot createRobot(AdminRobotCreateObj robotCreateObj) throws Exception {
        Long registrationId = robotCreateObj.getTeamRegistrationId();
        String name = robotCreateObj.getName();

        // overeni existence registrace tymu
        Optional<TeamRegistration> registrationOpt = this.teamRegistrationRepository.findById(registrationId);
        if (!registrationOpt.isPresent()) {
            throw new Exception(String.format("failure, team registration with ID [%d] not found", registrationId));
        }
        TeamRegistration registration = registrationOpt.get();

        // validace jmena robota
        validateRobotName(name);

        // overeni unikatnosti jmena robota v ramci rocniku souteze
        if (this.robotRepository.findByName(name).isPresent()) {
            throw new Exception(String.format("failure, robot with name [%s] already exists", name));
        }

        // vytvoreni robota
        Robot robot = new Robot(name, 0, registration);

        // pokud je specifikovana disciplina, registrujeme robota do ni
        if (robotCreateObj.getDisciplineId() != null) {
            Optional<Discipline> disciplineOpt = this.disciplineRepository.findById(robotCreateObj.getDisciplineId());
            if (!disciplineOpt.isPresent()) {
                throw new Exception(String.format("failure, discipline with ID [%d] not found", robotCreateObj.getDisciplineId()));
            }
            // Admin může registrovat i do skryté disciplíny - bez kontroly hidden
            robot.setDicipline(disciplineOpt.get());
        }

        return this.robotRepository.save(robot);
    }

    /**
     * Odstrani robota z registrace tymu
     * 
     * @param robotId ID robota
     * @throws Exception
     */
    @Transactional
    public void removeRobot(Long robotId) throws Exception {
        Optional<Robot> robotOpt = this.robotRepository.findById(robotId);
        if (!robotOpt.isPresent()) {
            throw new Exception(String.format("failure, robot with ID [%d] not found", robotId));
        }
        Robot robot = robotOpt.get();

        // kontrola ze robot neni potvrzeny (zachovani dat pro soutez)
        if (robot.getConfirmed()) {
            throw new Exception("failure, cannot remove robot because it is confirmed (data preservation)");
        }

        // smazani zapasu robota
        if (!robot.getMatches().isEmpty()) {
            this.robotMatchRepository.deleteAll(robot.getMatches());
        }

        this.robotRepository.delete(robot);
    }

    /**
     * Edituje udaje robota (admin muze menit cokoliv krome reference na registraci tymu)
     * 
     * @param robotId ID robota
     * @param editObj Nove udaje robota
     * @return Aktualizovany robot
     * @throws Exception
     */
    @Transactional
    public Robot editRobot(Long robotId, AdminRobotEditObj editObj) throws Exception {
        Optional<Robot> robotOpt = this.robotRepository.findById(robotId);
        if (!robotOpt.isPresent()) {
            throw new Exception(String.format("failure, robot with ID [%d] not found", robotId));
        }
        Robot robot = robotOpt.get();

        // zmena jmena
        if (editObj.getName() != null) {
            validateRobotName(editObj.getName());

            // overeni unikatnosti jmena v ramci rocniku
            Optional<Robot> existingRobot = this.robotRepository.findByName(editObj.getName());
            if (existingRobot.isPresent() && !existingRobot.get().getID().equals(robotId)) {
                throw new Exception("failure, robot with this name already exists");
            }

            robot.setName(editObj.getName());
        }

        // zmena cisla
        if (editObj.getNumber() != null) {
            robot.setNumber(editObj.getNumber());
        }

        // zmena discipliny
        if (editObj.getDisciplineId() != null) {
            if (editObj.getDisciplineId() == -1) {
                // zruseni registrace do discipliny
                robot.setDicipline(null);
            } else {
                Optional<Discipline> disciplineOpt = this.disciplineRepository.findById(editObj.getDisciplineId());
                if (!disciplineOpt.isPresent()) {
                    throw new Exception(String.format("failure, discipline with ID [%d] not found", editObj.getDisciplineId()));
                }
                // Admin může registrovat i do skryté disciplíny - bez kontroly hidden
                robot.setDicipline(disciplineOpt.get());
            }
        }

        // zmena stavu potvrzeni
        if (editObj.getConfirmed() != null) {
            robot.setConfirmed(editObj.getConfirmed());

            // pokud je registrace potvrzena a robot nema cislo, vygenerujeme ho
            if (editObj.getConfirmed() && robot.getNumber() == 0 && robot.getDiscipline() != null) {
                int year = robot.getTeamRegistration().getCompetitionYear();
                long maxNumber = 0;
                // najde maximalni cislo ze vsech robotu v danem rocniku
                List<Robot> allRobotsInYear = this.robotRepository.findAll().stream()
                        .filter(r -> r.getTeamRegistration().getCompetitionYear() == year)
                        .collect(java.util.stream.Collectors.toList());
                for (Robot r : allRobotsInYear) {
                    maxNumber = Math.max(maxNumber, r.getNumber());
                }
                robot.setNumber(maxNumber + 1);
            }
        }

        Robot savedRobot = this.robotRepository.save(robot);
        
        // Refresh Order Management System if running (to update robot name/info in queued matches)
        this.orderManagementService.refreshIfRunning();
        
        return savedRobot;
    }

    // ==================== FORCE OPERATIONS (pro vyjimecne situace) ====================

    /**
     * Forcne odstrani robota vcetne potvrzenho stavu (pouze pro vyjimecne situace!)
     * POZOR: Toto muze narusit integritu dat souteze!
     * 
     * @param robotId ID robota
     * @throws Exception
     */
    @Transactional
    public void forceRemoveRobot(Long robotId) throws Exception {
        Optional<Robot> robotOpt = this.robotRepository.findById(robotId);
        if (!robotOpt.isPresent()) {
            throw new Exception(String.format("failure, robot with ID [%d] not found", robotId));
        }
        Robot robot = robotOpt.get();

        // smazani zapasu robota
        if (!robot.getMatches().isEmpty()) {
            this.robotMatchRepository.deleteAll(robot.getMatches());
        }

        this.robotRepository.delete(robot);
    }

    /**
     * Forcne zmeni kategorii registrace tymu (pro vyjimecne situace)
     * 
     * @param registrationId ID registrace tymu
     * @param categoryName   Nova kategorie
     * @return Aktualizovana registrace
     * @throws Exception
     */
    @Transactional
    public TeamRegistration forceChangeCategory(Long registrationId, ECategory categoryName) throws Exception {
        Optional<TeamRegistration> registrationOpt = this.teamRegistrationRepository.findById(registrationId);
        if (!registrationOpt.isPresent()) {
            throw new Exception(String.format("failure, team registration with ID [%d] not found", registrationId));
        }
        TeamRegistration registration = registrationOpt.get();

        Optional<Category> categoryOpt = this.categoryRepository.findByName(categoryName);
        if (!categoryOpt.isPresent()) {
            throw new Exception(String.format("failure, category [%s] not found", categoryName));
        }

        registration.setCategory(categoryOpt.get());
        return this.teamRegistrationRepository.save(registration);
    }

    /**
     * Presune uzivatele z jednoho tymu do druheho (pro reseni konfliktů)
     * 
     * @param userId   ID uzivatele
     * @param newTeamId ID noveho tymu
     * @throws Exception
     */
    @Transactional
    public void transferUser(Long userId, Long newTeamId) throws Exception {
        Optional<UserRC> userOpt = this.userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new Exception(String.format("failure, user with ID [%d] not found", userId));
        }
        UserRC user = userOpt.get();

        Optional<Team> newTeamOpt = this.teamRepository.findById(newTeamId);
        if (!newTeamOpt.isPresent()) {
            throw new Exception(String.format("failure, team with ID [%d] not found", newTeamId));
        }
        Team newTeam = newTeamOpt.get();

        // odebereme z puvodniho tymu
        if (user.getTeamID() != Team.NOT_IN_TEAM) {
            Optional<Team> oldTeamOpt = this.teamRepository.findById(user.getTeamID());
            if (oldTeamOpt.isPresent()) {
                Team old = oldTeamOpt.get();
                // pokud byl vedoucim stareho tymu, zrusi vazbu
                if (old.getLeaderID() == user.getID()) {
                    old.setLeader(null);
                }
                old.removeMember(user);
                this.teamRepository.save(old);
            }
        }

        // pridame do noveho tymu
        newTeam.addMember(user);
        this.teamRepository.save(newTeam);
        this.userRepository.save(user);
    }

    /**
     * Nastavi noveho vedouciho tymu (pro reseni konfliktu kdy vedouci neni k dispozici)
     * 
     * @param teamId ID tymu
     * @param newLeaderId ID noveho vedouciho
     * @throws Exception
     */
    @Transactional
    public void setTeamLeader(Long teamId, Long newLeaderId) throws Exception {
        Optional<Team> teamOpt = this.teamRepository.findById(teamId);
        if (!teamOpt.isPresent()) {
            throw new Exception(String.format("failure, team with ID [%d] not found", teamId));
        }
        Team team = teamOpt.get();

        Optional<UserRC> newLeaderOpt = this.userRepository.findById(newLeaderId);
        if (!newLeaderOpt.isPresent()) {
            throw new Exception(String.format("failure, user with ID [%d] not found", newLeaderId));
        }
        UserRC newLeader = newLeaderOpt.get();

        // kontrola ze uzivatel je clenem tymu
        boolean isMember = team.getMembers().stream()
                .anyMatch(m -> m.getID() == newLeaderId);
        if (!isMember) {
            throw new Exception("failure, user must be a member of the team to become leader");
        }

        team.setLeader(newLeader);
        this.teamRepository.save(team);
    }

    /**
     * Forcne potvrdi nebo zrusi potvrzeni registrace robota (pro reseni problemu na soutezi)
     * 
     * @param robotId   ID robota
     * @param confirmed Novy stav potvrzeni
     * @throws Exception
     */
    @Transactional
    public Robot forceConfirmRobot(Long robotId, boolean confirmed) throws Exception {
        Optional<Robot> robotOpt = this.robotRepository.findById(robotId);
        if (!robotOpt.isPresent()) {
            throw new Exception(String.format("failure, robot with ID [%d] not found", robotId));
        }
        Robot robot = robotOpt.get();

        robot.setConfirmed(confirmed);

        // pokud je registrace potvrzena a robot nema cislo, vygenerujeme ho
        if (confirmed && robot.getNumber() == 0 && robot.getDiscipline() != null) {
            int year = robot.getTeamRegistration().getCompetitionYear();
            long maxNumber = 0;
            // najde maximalni cislo ze vsech robotu v danem rocniku
            List<Robot> allRobotsInYear = this.robotRepository.findAll().stream()
                    .filter(r -> r.getTeamRegistration().getCompetitionYear() == year)
                    .collect(java.util.stream.Collectors.toList());
            for (Robot r : allRobotsInYear) {
                maxNumber = Math.max(maxNumber, r.getNumber());
            }
            robot.setNumber(maxNumber + 1);
        }

        return this.robotRepository.save(robot);
    }

    // ==================== USER BAN OPERATIONS ====================

    /**
     * Zabanuje uzivatele - uzivatel nebude moci pristupovat do systemu
     * 
     * @param userId ID uzivatele
     * @return Zabanovany uzivatel
     * @throws Exception
     */
    @Transactional
    public UserRC banUser(Long userId) throws Exception {
        // Ziskej aktualne prihlaseneho uzivatele
        UserRC currentUser = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // Kontrola, ze uzivatel nebanuje sam sebe
        if (currentUser.getID() == userId) {
            throw new Exception("failure, you cannot ban yourself");
        }

        Optional<UserRC> userOpt = this.userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new Exception(String.format("failure, user with ID [%d] not found", userId));
        }
        UserRC user = userOpt.get();

        if (user.isBanned()) {
            throw new Exception("failure, user is already banned");
        }

        // Zneplatni token - okamzite odhlaseni
        user.setToken(null);
        user.setBanned(true);

        return this.userRepository.save(user);
    }

    /**
     * Odbanuje uzivatele - uzivatel bude moci opet pristupovat do systemu
     * 
     * @param userId ID uzivatele
     * @return Odbanovany uzivatel
     * @throws Exception
     */
    @Transactional
    public UserRC unbanUser(Long userId) throws Exception {
        Optional<UserRC> userOpt = this.userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new Exception(String.format("failure, user with ID [%d] not found", userId));
        }
        UserRC user = userOpt.get();

        if (!user.isBanned()) {
            throw new Exception("failure, user is not banned");
        }

        user.setBanned(false);

        return this.userRepository.save(user);
    }
}
