package com.finalproj.orbitflow.email.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

/**
 * 계정 활성화 시 최초 비밀번호 설정 DTO
 *
 * @author : seunga03
 * @filename : PasswordActivateReqDto
 * @since : 2026-01-02 금요일
 */
@Getter
public class PasswordActivateReqDto {

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&]).{8,}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8자 이상이어야 합니다."
    )
    private String password;
}