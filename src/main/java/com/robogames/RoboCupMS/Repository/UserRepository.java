package com.robogames.RoboCupMS.Repository;

import java.util.Optional;

import com.robogames.RoboCupMS.Entity.UserRC;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repozitar pro uzivatele
 */
@Repository
public interface UserRepository extends JpaRepository<UserRC, Long> {

    Optional<UserRC> findByEmail(String email);

    Boolean existsByEmail(String email);

    Optional<UserRC> findByToken(String token);

}
