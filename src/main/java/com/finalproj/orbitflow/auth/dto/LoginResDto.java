package com.finalproj.orbitflow.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : LoginResponse
 * @since : 2025-12-16 화요일
 */
@Getter
@AllArgsConstructor
public class LoginResDto {

    private String accessToken;
    private String refreshToken;
    private Instant refreshExpiresAt;

}