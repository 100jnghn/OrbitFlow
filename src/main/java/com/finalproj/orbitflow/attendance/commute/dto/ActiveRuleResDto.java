package com.finalproj.orbitflow.attendance.commute.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ActiveRuleResDto
 * @since : 2025. 12. 20. 토요일
 */


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ActiveRuleResDto {
    private LocalTime startTime;
    private LocalTime endTime;
    private String ruleType; // "DEFAULT" (기본), "EXCEPTION" (예외) 구분용

    // 기본 생성자 외에 편의를 위한 생성자
    public ActiveRuleResDto(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}