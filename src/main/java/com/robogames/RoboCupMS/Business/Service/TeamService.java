package com.robogames.RoboCupMS.Business.Service;

import java.util.List;
import java.util.Optional;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Business.Object.TeamObj;
import com.robogames.RoboCupMS.Entity.Robot;
import com.robogames.RoboCupMS.Entity.Team;
import com.robogames.RoboCupMS.Entity.TeamRegistration;
import com.robogames.RoboCupMS.Entity.UserRC;
import com.robogames.RoboCupMS.Repository.TeamRepository;
import com.robogames.RoboCupMS.Repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Zajistuje spravu tymu
 */
@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

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
            return team.get();
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
     * Vytvori novy tym. Uzivatel, ktery tym vytvari se stava jeho vedoucim.
     * 
     * @param teamObj Parametry noveho tymu
     * @throws Exception
     */
    public void create(TeamObj teamObj) throws Exception {
        UserRC leader = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // overi zda uzivatel jiz neni clenem tymu
        if (leader.getTeamID() != Team.NOT_IN_TEAM) {
            throw new Exception("failure, you are already a member of the team");
        }

        // overeni unikatnosti jmena
        if (this.teamRepository.findByName(teamObj.getName()).isPresent()) {
            throw new Exception("failure, team with this name already exists");
        }

        Team t = new Team(teamObj.getName(), leader);
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
                if (reg.getCompatition().getStarted()) {
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
    public void addMember(String uuid) throws Exception {
        UserRC leader = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<Team> t = this.teamRepository.findAllByLeader(leader).stream().findFirst();
        if (t.isPresent()) {
            // overi zda nebyl jiz prekrocen pocet clenu v tymu
            if (t.get().getMembers().size() >= GlobalConfig.MAX_TEAM_MEMBERS) {
                throw new Exception("failure, team is full");
            }

            Optional<UserRC> u = this.userRepository.findByUuid(uuid);
            if (u.isPresent()) {
                // overa zda jiz pridavany uzivatel neni v nejakem tymu
                if (u.get().getTeamID() != Team.NOT_IN_TEAM) {
                    throw new Exception("failure, user is already in team");
                }

                // prida uzivatele do tymu
                t.get().getMembers().add(u.get());
                u.get().setTeam(t.get());
                this.teamRepository.save(t.get());
                this.userRepository.save(u.get());
            } else {
                throw new Exception(String.format("failure, user with UUID [%s] not found", uuid));
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
    public void removeMember(String uuid) throws Exception {
        UserRC leader = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<Team> t = this.teamRepository.findAllByLeader(leader).stream().findFirst();
        if (t.isPresent()) {
            Optional<UserRC> u = this.userRepository.findByUuid(uuid);
            if (u.isPresent()) {
                t.get().getMembers().remove(u.get());
                u.get().setTeam(null);
                this.teamRepository.save(t.get());
                this.userRepository.save(u.get());
            } else {
                throw new Exception(String.format("failure, user with UUID [%s] not found", uuid));
            }
        } else {
            throw new Exception("failure, you are not the leader of any existing team");
        }
    }

    /**
     * Opusti tym, ve ktrem se prihlaseny uzivatel aktualne nachazi. Pokud tim kdo
     * opousti tym je jeho vedouci pak se automaticky urci novy vedouci.
     */
    public void leaveTeam() throws Exception {
        UserRC user = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // overi zda se v nejakem tymu nachazi
        if (user.getTeamID() == Team.NOT_IN_TEAM) {
            throw new Exception("failure, you are not member of any team");
        }

        // pokud je uzivatel vedoucim tymu, pak se vedouci musi zmeni na jineho z clenu.
        // pokud zde jis clen neni bude nastaven na null.
        Team team = user.getTeam();
        if (team.getLeaderID() == user.getID()) {
            team.getMembers().remove(user);
            if (team.getMembers().isEmpty()) {
                team.setLeader(null);
            } else {
                team.setLeader(team.getMembers().get(0));
            }
            this.teamRepository.save(team);
        }

        user.setTeam(null);
        this.userRepository.save(user);
    }

}
