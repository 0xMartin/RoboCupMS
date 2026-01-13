package com.robogames.RoboCupMS.Repository;

import java.util.Optional;

import com.robogames.RoboCupMS.Business.Enum.EScoreType;
import com.robogames.RoboCupMS.Entity.ScoreType;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for score types
 */
public interface ScoreTypeRepository extends JpaRepository<ScoreType, Long> {

    /**
     * Find score type by name
     * 
     * @param name The score type name
     * @return Optional containing the score type if found
     */
    Optional<ScoreType> findByName(EScoreType name);

}
