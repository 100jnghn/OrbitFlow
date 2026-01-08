package com.finalproj.orbitflow.attendance.leave.dto;

import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveSearchReqDto
 * @since : 2026. 1. 8. 목요일
 */

@Getter
@Setter
@ToString
public class LeaveSearchReqDto {

    private Integer year;           // 조회 연도
    private String typeName;        // 휴가 유형 이름 (예: 연차, 반차, 병가)
    private DocumentStatus status;  // 결재 상태 (APPROVED, REJECTED, PENDING 등)

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;    // 검색 시작일 (YYYY-MM-DD)

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;      // 검색 종료일 (YYYY-MM-DD)
}