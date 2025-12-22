package com.finalproj.orbitflow.auth.repository;

import com.finalproj.orbitflow.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : RefreshTokenRepository
 * @since : 2025-12-18 목요일
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    void deleteAllByEmployeeId(Long employeeId);

    @Modifying
    @Transactional
    @Query("delete from RefreshToken r where r.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();

}
