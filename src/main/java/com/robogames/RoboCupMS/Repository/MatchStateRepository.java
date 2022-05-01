package com.robogames.RoboCupMS.Repository;

import com.robogames.RoboCupMS.Entity.MatchState;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repozitar pro stavy, ve kterych se muze zapas nachazet
 */
public interface MatchStateRepository extends JpaRepository<MatchState, Long> {

}
