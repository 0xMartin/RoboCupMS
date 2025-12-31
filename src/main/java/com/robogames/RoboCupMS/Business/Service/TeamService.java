package com.robogames.RoboCupMS.Business.Service;

import java.util.List;
import java.util.Optional;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Business.Object.TeamObj;
import com.robogames.RoboCupMS.Entity.Robot;
import com.robogames.RoboCupMS.Entity.Team;
import com.robogames.RoboCupMS.Entity.TeamInvitation;
import com.robogames.RoboCupMS.Entity.TeamRegistration;
import com.robogames.RoboCupMS.Entity.UserRC;
import com.robogames.RoboCupMS.Repository.TeamInvitationRepository;
import com.robogames.RoboCupMS.Repository.TeamRepository;
import com.robogames.RoboCupMS.Repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Zajistuje spravu tymu
 */
@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamInvitationRepository invitationRepository;

    /**
     * Navrati info o tymu, ve kterem se prihlaseny uzivatel nachazi
     * 
     * @param id ID tymu
     * @return Tym, ve kterem se prihlaseny uzivatel nachazi
     * @throws Exception
     */
    public Team myTeam() throws Exception {
        UserRC user = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (user.getTeamID() != Team.NOT_IN_TEAM) {
            Optional<Team> team = this.teamRepository.findById(user.getTeamID());
            if (team.isPresent()) {
                return team.get();
            } else {
                throw new Exception(String.format("failure, team with ID [%d] not found", user.getTeamID()));
            }
        } else {
            throw new Exception("failure, you are not a member of any team");
        }
    }

    /**
     * Navrati info o tymu s konkretnim ID
     * 
     * @param id ID tymu
     * @return Tym s kokretim ID
     * @throws Exception
     */
    public Team findID(Long id) throws Exception {
        Optional<Team> team = this.teamRepository.findById(id);
        if (team.isPresent()) {
            return team.get();
        } else {
            throw new Exception(String.format("team with ID [%d] not found", id));
        }
    }

    /**
     * Navrati info o tymu s konkretnim jmenem
     * 
     * @param name Jmeno tymu
     * @return Tym s konkretim jmenem
     * @throws Exception
     */
    public Team findName(String name) throws Exception {
        Optional<Team> team = this.teamRepository.findByName(name);
        if (team.isPresent()) {
            return team.get();
        } else {
            throw new Exception(String.format("team with Name [%s] not found", name));
        }
    }

    /**
     * Navrati vsechny tymy
     * 
     * @return Seznam vsech tymu
     */
    public List<Team> getAll() {
        List<Team> all = this.teamRepository.findAll();
        return all;
    }

    /**
     * Overi, zda ma nazev tymu spravnou delku
     * 
     * @param name Nazev tymu
     * @throws Exception
     */
    private void validateName(String name) throws Exception {
        if (name.length() < GlobalConfig.MIN_TEAM_NAME_LENGTH) {
            throw new Exception("failure, name is too short");
        } else if (name.length() > GlobalConfig.MAX_TEAM_NAME_LENGTH) {
            throw new Exception("failure, name is too long");
        }
    }

    /**
     * Vytvori novy tym. Uzivatel, ktery tym vytvari se stava jeho vedoucim.
     * 
     * @param teamObj Parametry noveho tymu
     * @throws Exception
     */
    public void create(TeamObj teamObj) throws Exception {
        UserRC leader = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String name = teamObj.getName();

        // overi delku nazvu tymu
        validateName(name);

        // overi zda uzivatel jiz neni clenem tymu
        if (leader.getTeamID() != Team.NOT_IN_TEAM) {
            throw new Exception("failure, you are already a member of the team");
        }

        // overeni unikatnosti jmena
        if (this.teamRepository.findByName(name).isPresent()) {
            throw new Exception("failure, team with this name already exists");
        }

        Team t = new Team(name, leader);
        this.teamRepository.save(t);
        this.userRepository.save(leader);
    }

    /**
     * Odstrani tym z databaze
     * 
     * @throws Exception
     */
    public void remove() throws Exception {
        UserRC leader = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<Team> t = this.teamRepository.findAllByLeader(leader).stream().findFirst();
        if (t.isPresent()) {
            for (TeamRegistration reg : t.get().getRegistrations()) {
                // overi zda jiz tento tym neni registrovan v nejakem rocnik, ktery jit zacal.
                // Pak v tom pripade neni mozne jiz tym odstranit, jelikoz system zaznamenava i
                // zapasy z minulych rocniku
                if (reg.getCompetition().getStarted()) {
                    throw new Exception(
                            "failure, it is not possible to remove the team because it is already registred in a competition that has already started");
                }
                // overi zda jiz tym nema nejakeho robota, ktery ma jiz potvrzenou registraci
                for (Robot r : reg.getRobots()) {
                    if (r.getConfirmed()) {
                        throw new Exception(
                                "failure, it is not possible to remove the team because it already have confirmed robot");
                    }
                }
            }

            // odebere cleny z tymu
            t.get().getMembers().forEach((m) -> {
                m.setTeam(null);
            });
            this.userRepository.saveAll(t.get().getMembers());

            // odstrani tym
            this.teamRepository.delete(t.get());
        } else {
            throw new Exception("failure, you are not the leader of any existing team");
        }
    }

    /**
     * Prejmenuje tym (pouze vedouci tymu)
     * 
     * @param name Nove jmeno tymu
     * @throws Exception
     */
    public void rename(String name) throws Exception {
        UserRC leader = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // overi delku nazvu tymu
        validateName(name);

        // overeni unikatnosti nazvu tymu
        if(this.teamRepository.findByName(name).isPresent()) {
            throw new Exception("failure, team with this name already exists");
        }

        Optional<Team> t = this.teamRepository.findAllByLeader(leader).stream().findFirst();
        if (t.isPresent()) {
            t.get().setName(name);
            this.teamRepository.save(t.get());
        } else {
            throw new Exception("failure, you are not the leader of any existing team");
        }
    }

    /**
     * Prida do tymu noveho clena
     * 
     * @param id ID clena, ktery ma byt pridat do tymu
     * @throws Exception
     */
    public void addMember(Long id) throws Exception {
        UserRC leader = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<Team> t = this.teamRepository.findAllByLeader(leader).stream().findFirst();
        if (t.isPresent()) {
            // overi zda nebyl jiz prekrocen pocet clenu v tymu
            if (t.get().getMembers().size() >= GlobalConfig.MAX_TEAM_MEMBERS) {
                throw new Exception("failure, team is full");
            }

            Optional<UserRC> u = this.userRepository.findById(id);

            // overi zda uzivatel jeste nebyl do tymu pozvan
            Optional<TeamInvitation> existingInvitation = this.invitationRepository.findByUserAndTeam(u.get(), t.get());
            if (existingInvitation.isPresent()) {
                throw new Exception("failure, user already invited");
            }

            if (u.isPresent()) {
                // vytvori pozvanku do tymu
                TeamInvitation invitation = new TeamInvitation();
                invitation.setUser(u.get());
                invitation.setTeam(t.get());
                this.invitationRepository.save(invitation);
            } else {
                throw new Exception(String.format("failure, user with ID [%s] not found", id));
            }
        } else {
            throw new Exception("failure, you are not the leader of any existing team");
        }
    }

    /**
     * Odebere z tymu jednoho clena
     * 
     * @param id ID clena, ktery ma byt odebran z tymu
     * @throws Exception
     */
    public void removeMember(Long id) throws Exception {
        UserRC leader = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<Team> t = this.teamRepository.findAllByLeader(leader).stream().findFirst();
        if (t.isPresent()) {
            Optional<UserRC> u = this.userRepository.findById(id);
            if (u.isPresent()) {
                t.get().getMembers().remove(u.get());
                u.get().setTeam(null);
                this.teamRepository.save(t.get());
                this.userRepository.save(u.get());
            } else {
                throw new Exception(String.format("failure, user with ID [%s] not found", id));
            }
        } else {
            throw new Exception("failure, you are not the leader of any existing team");
        }
    }

    public void acceptInvitation(Long id) throws Exception {
        Optional<TeamInvitation> invitation = this.invitationRepository.findById(id);
        if (invitation.isPresent()) {
            UserRC currentUser = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserRC u = invitation.get().getUser();
            Team t = invitation.get().getTeam();

            // pokud pozvanka nepatri uzivateli, kteremu realne byla odeslana
            if (currentUser.getID() != u.getID()) {
                throw new Exception("failure, this is not your invitation");
            }

            // prida uzivatele do tymu a ulozi zmeni v databazi pro tym i pro uzivatele
            t.getMembers().add(u);
            u.setTeam(t);
            this.teamRepository.save(t);
            this.userRepository.save(u);

            // odstraneni z databaze
            this.invitationRepository.delete(invitation.get());
        } else {
            throw new Exception(String.format("failure, invitation with ID [%s] not found", id));
        }
    }

    public void rejectInvitation(Long id) throws Exception {
        Optional<TeamInvitation> invitation = this.invitationRepository.findById(id);
        if (invitation.isPresent()) {
            UserRC currentUser = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserRC u = invitation.get().getUser();

            // pokud pozvanka nepatri uzivateli, kteremu realne byla odeslana
            if (currentUser.getID() != u.getID()) {
                throw new Exception("failure, this is not your invitation");
            }

            // odstraneni z databaze
            this.invitationRepository.delete(invitation.get());
        } else {
            throw new Exception(String.format("failure, invitaton with ID [%s] not found", id));
        }
    }

    /**
     * Opusti tym, ve ktrem se prihlaseny uzivatel aktualne nachazi. Pokud tim kdo
     * opousti tym je jeho vedouci pak se automaticky urci novy vedouci.
     */
    @Transactional
    public void leaveTeam() throws Exception {
        UserRC user = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // overi zda se v nejakem tymu nachazi
        if (user.getTeamID() == Team.NOT_IN_TEAM) {
            throw new Exception("failure, you are not member of any team");
        }

        // pokud je uzivatel vedoucim tymu, pak se vedoucim musi stat jiny z clenu tymu.
        // pokud zde jiz zadny clen neni, bude nastaven na null.
        Team team = user.getTeam();
        if (team.getLeaderID() == user.getID()) {
            List<UserRC> members = userRepository.findByTeamId(team.getID());

            if (members.size() <= 1) {
                team.setLeader(null);
            } else {
                UserRC newLeader = members.stream()
                        .filter(u -> u.getID() != user.getID())
                        .findFirst()
                        .orElse(null);
                team.setLeader(newLeader);
            }
            this.teamRepository.save(team);
        }

        user.setTeam(null);
        this.userRepository.save(user);
    }

}
