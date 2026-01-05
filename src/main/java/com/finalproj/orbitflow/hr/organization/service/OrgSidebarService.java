package com.finalproj.orbitflow.hr.organization.service;

import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.hr.organization.dto.sidebar.OrgSidebarDto;
import com.finalproj.orbitflow.hr.organization.dto.sidebar.OrgSidebarEmployeeDto;
import com.finalproj.orbitflow.hr.organization.entity.Organization;
import com.finalproj.orbitflow.hr.organization.repository.OrgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgSidebarService
 * @since : 2026-01-05 월요일
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrgSidebarService {

    private final OrgRepository orgRepository;
    private final EmployeeRepository employeeRepository;

    public List<OrgSidebarDto> getSidebarTree(Long companyId) {
        // 조직 전체 조회 (ACTIVE)
        List<Organization> orgs =
                orgRepository.findByCompanyIdAndIsActiveTrueOrderByParentOrgIdAscOrderIndexAsc(companyId);

        // 내선 있는 사원 조회
        List<Employee> employees =
                employeeRepository.findActiveWithExtension(companyId);

        // orgId 기준 사원 그룹핑
        Map<Long, List<OrgSidebarEmployeeDto>> employeeMap =
                employees.stream()
                        .collect(Collectors.groupingBy(
                                e -> e.getOrganization().getId(),
                                Collectors.mapping(
                                        e -> new OrgSidebarEmployeeDto(
                                                e.getId(),
                                                e.getName(),
                                                e.getInternalPhone()
                                        ),
                                        Collectors.toList()
                                )
                        ));

        // orgId -> OrgSidebarDto Map 생성
        Map<Long, OrgSidebarDto> orgDtoMap = new HashMap<>();

        for (Organization org : orgs) {
            orgDtoMap.put(
                    org.getId(),
                    new OrgSidebarDto(
                            org.getId(),
                            org.getName(),
                            employeeMap.getOrDefault(org.getId(), List.of()),
                            new ArrayList<>()
                    )
            );
        }

        // 트리 구성 (부모 -> 자식)
        List<OrgSidebarDto> roots = new ArrayList<>();

        for (Organization org : orgs) {
            OrgSidebarDto current = orgDtoMap.get(org.getId());

            if (org.getParentOrgId() == null) {
                roots.add(current);
            } else {
                OrgSidebarDto parent = orgDtoMap.get(org.getParentOrgId());
                if (parent != null) {
                    parent.getChildren().add(current);
                }
            }
        }


        return roots;

    }
}
