package com.robogames.RoboCupMS.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.robogames.RoboCupMS.Entity.Team;
import com.robogames.RoboCupMS.Entity.TeamInvitation;
import com.robogames.RoboCupMS.Entity.UserRC;

/**
 * Repozitar pro vsechny pozvanky do tymu
 */
public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, Long> {
    Optional<TeamInvitation> findByUserAndTeam(UserRC u, Team t);
    
    /**
     * Najde vsechny pozvanky pro uzivatele serazene od nejstarsi
     */
    List<TeamInvitation> findByUserOrderByCreatedAtAsc(UserRC user);
}
