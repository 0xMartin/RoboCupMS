package com.robogames.RoboCupMS.Repository;

import java.util.Optional;

import com.robogames.RoboCupMS.Business.Enum.ETournamentPhase;
import com.robogames.RoboCupMS.Entity.TournamentPhase;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for tournament phases
 */
public interface TournamentPhaseRepository extends JpaRepository<TournamentPhase, Long> {

    /**
     * Find tournament phase by name
     * 
     * @param name The phase name
     * @return Optional containing the tournament phase if found
     */
    Optional<TournamentPhase> findByName(ETournamentPhase name);

}
