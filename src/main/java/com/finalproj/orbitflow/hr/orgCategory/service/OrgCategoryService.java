package com.finalproj.orbitflow.hr.orgCategory.service;


import com.finalproj.orbitflow.global.exception.BusinessException;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.InvalidStateException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.hr.logAudit.enums.AuditEntityType;
import com.finalproj.orbitflow.hr.logAudit.enums.AuditEventType;
import com.finalproj.orbitflow.hr.logAudit.service.AuditLogService;
import com.finalproj.orbitflow.hr.orgCategory.dto.OrgCategoryOrderUpdateReqDto;
import com.finalproj.orbitflow.hr.orgCategory.dto.OrgCategoryResDto;
import com.finalproj.orbitflow.hr.orgCategory.dto.OrgCategoryUpdateReqDto;
import com.finalproj.orbitflow.hr.orgCategory.entity.OrgCategory;
import com.finalproj.orbitflow.hr.orgCategory.repository.OrgCategoryRepository;
import com.finalproj.orbitflow.hr.organization.repository.OrgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgCategoryService
 * @since : 2025-12-17 수요일
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrgCategoryService {

    private final OrgCategoryRepository repository;
    private final OrgRepository orgRepository;
    private final AuditLogService auditLogService;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;

    /* ================= 목록 (검색 포함) ================= */
    @Transactional(readOnly = true)
    public List<OrgCategoryResDto> findAll(Long companyId, String keyword, boolean includeInactive) {

        List<OrgCategory> categories;

        if (includeInactive) {
            categories = repository.findByCompanyIdOrderByIsActiveDescOrderIndexAscCreatedAtDesc(companyId);
        } else {
            categories =
                    (keyword == null || keyword.isBlank())
                            ? repository.findByCompanyIdAndIsActiveTrueOrderByOrderIndexAsc(companyId)
                            : repository.findByCompanyIdAndIsActiveTrueAndNameContainingIgnoreCaseOrderByOrderIndexAsc(
                            companyId, keyword.trim());
        }

        return categories.stream()
                .map(OrgCategoryResDto::from)
                .toList();
    }

    /* ================= 생성 ================= */
    public Long create(Long companyId, String rawName) {

        String name = rawName.trim();

        if (repository.existsByCompanyIdAndName(companyId, name)) {
            throw new BusinessException("이미 존재하는 조직 카테고리입니다.");
        }

        // 회사 루트는 수동 생성 금지
        if (repository.existsByCompanyIdAndIsRootTrue(companyId)
                && "회사".equals(name)) {
            throw new InvalidRequestException("회사 카테고리는 시스템에서 자동 생성됩니다.");
        }

        Integer max = repository.findMaxActiveOrderIndexByCompanyId(companyId);
        int nextOrder = (max == null) ? 1 : max + 1;

        OrgCategory saved = repository.save(
                OrgCategory.create(companyId, name, nextOrder, false)
        );

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalStateException("회사 정보 없음"));

        Employee actor = employeeRepository.findById(
                SecurityUtils.getEmployeeId()
        ).orElseThrow(() -> new IllegalStateException("행위자 사원 정보 없음"));

        auditLogService.log(
                company,
                actor,
                AuditEntityType.ORG_CATEGORY,
                saved.getId(),
                AuditEventType.CREATE,
                null,
                Map.of(
                        "name", saved.getName(),
                        "orderIndex", saved.getOrderIndex()
                )
        );

        return saved.getId();

    }

    /* ================= 수정 (이름 + 활성/비활성/재활성화) ================= */
    public void update(Long companyId, Long id, OrgCategoryUpdateReqDto request) {
        OrgCategory category = get(companyId, id);

        if (category.getIsRoot()) {
            throw new InvalidStateException("회사 카테고리는 수정할 수 없습니다.");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalStateException("회사 정보 없음"));

        Employee actor = employeeRepository.findById(
                SecurityUtils.getEmployeeId()
        ).orElseThrow(() -> new IllegalStateException("행위자 사원 정보 없음"));

        String newName = request.getName().trim();
        String oldName = category.getName();

        if (repository.existsByCompanyIdAndNameAndIdNot(companyId, newName, id)) {
            throw new BusinessException("이미 존재하는 조직 카테고리입니다.");
        }

        boolean wasInactive = !category.getIsActive();
        boolean willActivate = Boolean.TRUE.equals(request.getIsActive());
        boolean willDeactivate = Boolean.FALSE.equals(request.getIsActive());

        /* ===== 재활성화 ===== */
        if (wasInactive && willActivate) {
            Integer max = repository.findMaxActiveOrderIndexByCompanyId(companyId);
            category.activate(max == null ? 1 : max + 1);

            auditLogService.log(
                    company,
                    actor,
                    AuditEntityType.ORG_CATEGORY,
                    category.getId(),
                    AuditEventType.ACTIVATE,
                    Map.of("isActive", false),
                    Map.of("isActive", true)
            );
        }

        /* ===== 비활성화 ===== */
        if (willDeactivate) {

            if (orgRepository.existsByCompanyIdAndCategoryIdAndIsActiveTrue(companyId, id)) {
                throw new InvalidStateException(
                        "해당 카테고리를 사용하는 조직이 존재하여 비활성화할 수 없습니다."
                );
            }

            category.deactivate();

            auditLogService.log(
                    company,
                    actor,
                    AuditEntityType.ORG_CATEGORY,
                    category.getId(),
                    AuditEventType.DEACTIVATE,
                    Map.of("isActive", true),
                    Map.of("isActive", false)
            );
        }

        /* ===== 이름 변경 ===== */
        if (!oldName.equals(newName)) {
            category.updateName(newName);

            auditLogService.log(
                    company,
                    actor,
                    AuditEntityType.ORG_CATEGORY,
                    category.getId(),
                    AuditEventType.UPDATE,
                    Map.of("name", oldName),
                    Map.of("name", newName)
            );
        }
    }


    /* ================= 순서 변경 ================= */
    public void updateOrder(Long companyId, List<OrgCategoryOrderUpdateReqDto.OrderItem> orders) {

        if (orders == null || orders.isEmpty()) {
            throw new InvalidRequestException("순서 정보가 비어 있습니다.");
        }

        long activeCount = repository.countByCompanyIdAndIsActiveTrueAndIsRootFalse(companyId); // 루트 카테고리는 카운트에서 제외
        if (activeCount != orders.size()) {
            throw new InvalidStateException("정렬 대상 개수가 일치하지 않습니다.");
        }

        // 1단계: 임시 order_index (충돌 방지)
        int temp = -1;
        for (var item : orders) {
            OrgCategory category = get(companyId, item.getId());

            if (category.getIsRoot()) {
                throw new InvalidStateException("회사 카테고리는 정렬할 수 없습니다.");
            }

            category.updateOrderIndex(temp--);
        }
        repository.flush();

        // 2단계: 최종 order_index
        int index = 1;
        for (var item : orders) {
            get(companyId, item.getId()).updateOrderIndex(index++);
        }
    }

    private OrgCategory get(Long companyId, Long id) {
        return repository.findByCompanyIdAndId(companyId, id)
                .orElseThrow(() -> new NotFoundException("조직 카테고리를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<OrgCategoryResDto> findSelectableForOrg(Long companyId) {
        return repository
                .findByCompanyIdAndIsActiveTrueAndIsRootFalseOrderByOrderIndexAsc(companyId)
                .stream()
                .map(OrgCategoryResDto::from)
                .toList();
    }
}
