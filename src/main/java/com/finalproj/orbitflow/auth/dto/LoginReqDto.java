package com.finalproj.orbitflow.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : LoginRequest
 * @since : 2025-12-16 화요일
 */
@Getter
@NoArgsConstructor
public class LoginReqDto {

    private String email;
    private String password;
}
