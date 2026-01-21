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

    private Integer year;
    private String typeName;
    private DocumentStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}