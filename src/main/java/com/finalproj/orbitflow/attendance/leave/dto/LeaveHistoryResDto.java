package com.finalproj.orbitflow.attendance.leave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveHistoryResDto
 * @since : 2025. 12. 27. 토요일
 */

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveHistoryResDto {
    private String title;
    private String actionDate;
    private String period;
    private BigDecimal days;
    private String type;
    private String statusName;
    private String statusCode;
    private String reason;
    private String typeDescription;
}