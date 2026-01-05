package com.finalproj.orbitflow.hr.organization.dto.sidebar;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgSidebarDto
 * @since : 2026-01-05 월요일
 */
@Getter
@AllArgsConstructor
public class OrgSidebarDto {

    private Long orgId;
    private String orgName;

    private List<OrgSidebarEmployeeDto> employees;   // 이 조직 소속 사원
    private List<OrgSidebarDto> children;             // 하위 조직
}
