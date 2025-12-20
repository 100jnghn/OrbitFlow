package com.finalproj.orbitflow.hr.rank.dto;

import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : RankUpdateReqDto
 * @since : 2025-12-20 토요일
 */
@Getter
public class RankUpdateReqDto {
    private String name;
    private Long parentRankId; // nullable
    private Boolean isActive;
}
