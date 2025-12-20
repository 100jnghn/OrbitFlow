package com.finalproj.orbitflow.hr.organization.service;

import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.InvalidStateException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.hr.organization.dto.OrgCreateReqDto;
import com.finalproj.orbitflow.hr.organization.dto.OrgOrderUpdateReqDto;
import com.finalproj.orbitflow.hr.organization.dto.OrgResDto;
import com.finalproj.orbitflow.hr.organization.dto.OrgUpdateReqDto;
import com.finalproj.orbitflow.hr.organization.entity.Organization;
import com.finalproj.orbitflow.hr.organization.repository.OrgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgService
 * @since : 2025-12-19 금요일
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrgService {

    private final OrgRepository orgRepository;
    private final EmployeeRepository employeeRepository;

    /* ================= 생성 ================= */
    public Long create(Long companyId, OrgCreateReqDto request) {

        long count =
                orgRepository
                        .countByCompanyIdAndParentOrgIdAndIsActiveTrue(
                                companyId,
                                request.getParentOrgId()
                        );

        int nextOrderIndex = (int) count + 1;

        Organization org = Organization.create(
                companyId,
                request.getCategoryId(),
                request.getParentOrgId(),
                request.getName(),
                nextOrderIndex
        );

        return orgRepository.save(org).getId();
    }

    /* ================= 조회 ================= */
    @Transactional(readOnly = true)
    public List<OrgResDto> findAll(Long companyId) {

        return orgRepository
                .findByCompanyIdAndIsActiveTrueOrderByOrderIndexAsc(companyId)
                .stream()
                .map(OrgResDto::from)
                .toList();
    }

    /* ================= 수정 ================= */
    public void update(
            Long companyId,
            Long organizationId,
            OrgUpdateReqDto request
    ) {

        Organization org = orgRepository
                .findByCompanyIdAndId(companyId, organizationId)
                .orElseThrow(() ->
                        new NotFoundException("조직을 찾을 수 없습니다.")
                );

        org.update(
                request.getCategoryId(),
                request.getParentOrgId(),
                request.getName()
        );
    }

    /* ================= 비활성화 ================= */
    public void deactivate(Long companyId, Long organizationId) {

        Organization org = orgRepository
                .findByCompanyIdAndId(companyId, organizationId)
                .orElseThrow(() ->
                        new NotFoundException("조직을 찾을 수 없습니다.")
                );

        // 조직에 재직/정지/임시 계정이 남아 있으면 비활성화 불가
        if (employeeRepository.existsByCompanyIdAndOrganizationIdAndStatusNot(
                companyId,
                organizationId,
                EmployeeStatus.RESIGNED
        )) {
            throw new InvalidStateException(
                    "해당 조직에 소속된 재직 중인 사원이 존재하여 비활성화할 수 없습니다."
            );
        }

        org.deactivate();
    }



    /* ================= 정렬 ================= */
    public void updateOrder(
            Long companyId,
            List<OrgOrderUpdateReqDto.OrderItem> orders
    ) {
        // 기본 검증
        if (orders == null || orders.isEmpty()) {
            throw new InvalidRequestException("순서 정보가 비어 있습니다.");
        }

        // 같은 parentOrgId인지 검증
        Long parentOrgId = null;

        for (OrgOrderUpdateReqDto.OrderItem item : orders) {
            Organization org = orgRepository
                    .findByCompanyIdAndId(companyId, item.getId())
                    .orElseThrow(() ->
                            new ForbiddenException("해당 회사의 조직이 아닙니다.")
                    );

            if (parentOrgId == null) {
                parentOrgId = org.getParentOrgId();
            } else if (!Objects.equals(parentOrgId, org.getParentOrgId())) {
                throw new InvalidStateException(
                        "서로 다른 상위 조직의 조직은 함께 정렬할 수 없습니다."
                );
            }
        }

        // 정렬 대상 개수 검증
        long activeCount =
                orgRepository.countByCompanyIdAndParentOrgIdAndIsActiveTrue(
                        companyId, parentOrgId
                );

        if (activeCount != orders.size()) {
            throw new InvalidStateException("정렬 대상 개수가 일치하지 않습니다.");
        }

        // 임시 orderIndex 할당
        int tempIndex = -1;
        for (OrgOrderUpdateReqDto.OrderItem item : orders) {
            Organization org = orgRepository
                    .findByCompanyIdAndId(companyId, item.getId())
                    .orElseThrow(() ->
                            new ForbiddenException("해당 회사의 조직이 아닙니다.")
                    );
            org.updateOrderIndex(tempIndex--);
        }

        orgRepository.flush();

        // 최종 orderIndex 재할당
        int orderIndex = 1;
        for (OrgOrderUpdateReqDto.OrderItem item : orders) {
            Organization org = orgRepository
                    .findByCompanyIdAndId(companyId, item.getId())
                    .orElseThrow(() ->
                            new ForbiddenException("해당 회사의 조직이 아닙니다.")
                    );
            org.updateOrderIndex(orderIndex++);
        }
    }

}
