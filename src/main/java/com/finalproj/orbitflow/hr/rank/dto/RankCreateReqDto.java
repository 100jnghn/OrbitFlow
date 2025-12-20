package com.finalproj.orbitflow.hr.rank.dto;

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
    private String name;
    private Long parentRankId; // nullable
}
