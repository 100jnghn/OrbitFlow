package com.finalproj.orbitflow.hr.employee.dto;

import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : EmployeeListResDto
 * @since : 2025-12-29 월요일
 */
@Getter
@AllArgsConstructor
public class EmployeeListResDto {

    private Long id;
    private String name;
    private String email;
    private String orgPath;        // 경영지원본부 > 인사팀
    private String rankName;       // 대리
    private String positionName;   // 팀장
    private EmployeeStatus status;

}
