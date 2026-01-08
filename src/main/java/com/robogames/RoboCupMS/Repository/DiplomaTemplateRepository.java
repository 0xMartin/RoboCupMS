package com.robogames.RoboCupMS.Repository;

import com.robogames.RoboCupMS.Entity.DiplomaTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repozitář pro template diplomů
 */
@Repository
public interface DiplomaTemplateRepository extends JpaRepository<DiplomaTemplate, Long> {

    /**
     * Najde první template v databázi (pro případ, že by jich bylo více)
     * @return První template
     */
    Optional<DiplomaTemplate> findFirstByOrderByIdDesc();
}