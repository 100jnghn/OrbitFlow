package com.finalproj.orbitflow.hr.rank.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : RankResDto
 * @since : 2025-12-20 토요일
 */
@Getter
@Builder
public class RankResDto {
    private Long id;
    private String name;

    private Long parentRankId;
    private String parentRankName;

    private Integer orderIndex;
    private Boolean isActive;

    private Long employeeCount; // 부여 인원
}