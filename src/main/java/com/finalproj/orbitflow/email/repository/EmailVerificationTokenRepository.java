package com.finalproj.orbitflow.email.repository;

import com.finalproj.orbitflow.email.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : EmailVerificationTokenRepository
 * @since : 2026-01-01 목요일
 */
public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);
}
