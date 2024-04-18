package com.robogames.RoboCupMS.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.robogames.RoboCupMS.Entity.TeamInvitation;

/**
 * Repozitar pro vsechny pozvanky do tymu
 */
public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, Long> {
    
}
