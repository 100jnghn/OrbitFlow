package com.finalproj.orbitflow.hr.rank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : RankCreateReqDto
 * @since : 2025-12-20 토요일
 */
@Getter
public class RankCreateReqDto {
    @NotBlank(message = "직급명은 필수입니다.")
    @Size(max = 50, message = "직급명은 50자 이하여야 합니다.")
    private String name;
    private Long parentRankId; // nullable
}
