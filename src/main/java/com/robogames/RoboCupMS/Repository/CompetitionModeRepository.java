package com.robogames.RoboCupMS.Repository;

import java.util.Optional;

import com.robogames.RoboCupMS.Business.Enum.ECompetitionMode;
import com.robogames.RoboCupMS.Entity.CompetitionMode;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for competition modes
 */
public interface CompetitionModeRepository extends JpaRepository<CompetitionMode, Long> {

    /**
     * Find competition mode by name
     * 
     * @param name The competition mode name
     * @return Optional containing the competition mode if found
     */
    Optional<CompetitionMode> findByName(ECompetitionMode name);

}
