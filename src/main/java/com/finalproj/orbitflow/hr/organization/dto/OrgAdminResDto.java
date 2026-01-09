package com.finalproj.orbitflow.hr.organization.dto;

import com.finalproj.orbitflow.hr.organization.entity.Organization;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgAdminResDto
 * @since : 2026-01-08 목요일
 */
@Getter
@AllArgsConstructor
public class OrgAdminResDto {

    private Long id;
    private Long categoryId;
    private Long parentOrgId;
    private String name;
    private Integer orderIndex;
    private Boolean isActive;
    private Boolean isRootOrg;

    private Long employeeCount;     // 소속 사원 수
    private Long childOrgCount;     // 직계 하위 조직 수

    public static OrgAdminResDto from(
            Organization o,
            long employeeCount,
            long childOrgCount
    ) {
        return new OrgAdminResDto(
                o.getId(),
                o.getCategoryId(),
                o.getParentOrgId(),
                o.getName(),
                o.getOrderIndex(),
                o.getIsActive(),
                o.getParentOrgId() == null,
                employeeCount,
                childOrgCount
        );
    }
}
