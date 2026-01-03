package com.finalproj.orbitflow.email.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : PasswordResetReqDto
 * @since : 2026-01-02 금요일
 */
@Getter
public class PasswordResetReqDto {

    @NotBlank
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;
}