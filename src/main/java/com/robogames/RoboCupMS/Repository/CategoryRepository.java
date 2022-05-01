package com.robogames.RoboCupMS.Repository;

import java.util.Optional;

import com.robogames.RoboCupMS.Entity.Category;
import com.robogames.RoboCupMS.Enum.ECategory;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repozitar pro kategorie tymu
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(ECategory name);

}
