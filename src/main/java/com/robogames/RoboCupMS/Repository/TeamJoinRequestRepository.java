package com.robogames.RoboCupMS.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.robogames.RoboCupMS.Entity.Team;
import com.robogames.RoboCupMS.Entity.TeamJoinRequest;
import com.robogames.RoboCupMS.Entity.UserRC;

/**
 * Repozitar pro zadosti o vstup do tymu
 */
@Repository
public interface TeamJoinRequestRepository extends JpaRepository<TeamJoinRequest, Long> {
    
    /**
     * Najde zadost od konkretniho uzivatele do konkretniho tymu
     */
    Optional<TeamJoinRequest> findByUserAndTeam(UserRC user, Team team);
    
    /**
     * Najde vsechny zadosti pro dany tym (serazene od nejstarsi)
     */
    List<TeamJoinRequest> findByTeamOrderByCreatedAtAsc(Team team);
    
    /**
     * Najde vsechny zadosti od konkretniho uzivatele
     */
    List<TeamJoinRequest> findByUser(UserRC user);
    
    /**
     * Najde vsechny zadosti pro tym podle ID tymu
     */
    List<TeamJoinRequest> findByTeam_IdOrderByCreatedAtAsc(Long teamId);
    
    /**
     * Smaze vsechny zadosti o vstup do konkretniho tymu
     */
    void deleteByTeam(Team team);
}
