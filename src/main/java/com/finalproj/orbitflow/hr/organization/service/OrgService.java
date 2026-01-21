package com.finalproj.orbitflow.hr.organization.service;

import com.finalproj.orbitflow.board.boardcategory.service.OrganizationBoardCategorySyncService;
import com.finalproj.orbitflow.global.exception.ForbiddenException;
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
import com.finalproj.orbitflow.hr.orgCategory.repository.OrgCategoryRepository;
import com.finalproj.orbitflow.hr.orgPositionUsage.repository.OrgPositionUsageRepository;
import com.finalproj.orbitflow.hr.organization.dto.*;
import com.finalproj.orbitflow.hr.organization.entity.Organization;
import com.finalproj.orbitflow.hr.organization.repository.OrgRepository;
import com.finalproj.orbitflow.hr.organization.repository.OrgResView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
@Slf4j
public class OrgService {

    private final OrgRepository orgRepository;
    private final EmployeeRepository employeeRepository;
    private final OrgCategoryRepository orgCategoryRepository;
    private final OrgPositionUsageRepository orgPositionUsageRepository;
    private final OrganizationBoardCategorySyncService organizationBoardCategorySyncService;
    private final AuditLogService auditLogService;
    private final CompanyRepository companyRepository;

    /* ================= 생성 ================= */
    public Long create(Long companyId, OrgCreateReqDto request) {

        Long parentOrgId = request.getParentOrgId();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalStateException("회사 정보 없음"));

        // 최상위 조직 생성 차단
        if (parentOrgId == null) {
            throw new InvalidRequestException(
                    "최상위 조직(회사)은 시스템에서 자동 생성되며 직접 추가할 수 없습니다."
            );
        }

        Long categoryId = request.getCategoryId();
        String name = normalizeNameOrThrow(request.getName());

        // 카테고리 검증(존재 + 같은 회사 + 활성)
        validateCategoryActiveInCompany(companyId, categoryId);

        // 상위 조직 검증(있다면 존재 + 같은 회사 + 활성)
        getActiveOrgInCompanyOrThrow(companyId, parentOrgId); // parent 변수는 검증용 (미사용이어도 OK)

        // 형제 단위 이름 중복(활성 기준)
        if (orgRepository.existsByCompanyIdAndParentOrgIdAndNameAndIsActiveTrue(companyId, parentOrgId, name)) {
            throw new InvalidStateException("이미 존재하는 조직명입니다.");
        }

        // 다음 orderIndex (형제 단위)
        int nextOrderIndex =
                (int) orgRepository.countByCompanyIdAndParentOrgIdAndIsActiveTrue(
                        companyId, parentOrgId) + 1;

        Organization org =
                Organization.create(companyId, categoryId, parentOrgId, name, nextOrderIndex);

        Organization saved = orgRepository.save(org);

        Employee actor = employeeRepository.findById(
                SecurityUtils.getEmployeeId()
        ).orElseThrow(() -> new IllegalStateException("행위자 사원 정보 없음"));

        Map<String, Object> after = new java.util.HashMap<>();
        after.put("name", saved.getName());
        after.put("parentOrgId", saved.getParentOrgId());
        after.put("categoryId", saved.getCategoryId());

        auditLogService.log(
                company,
                actor,
                AuditEntityType.ORGANIZATION,
                saved.getId(),
                AuditEventType.CREATE,
                null,
                after
        );


        // 조직 게시판 카테고리 자동 생성
        organizationBoardCategorySyncService.createIfAbsent(
                companyId, saved.getId(), saved.getName());

        return saved.getId();
    }

    /* ================= 조회 ================= */
    @Transactional(readOnly = true)
    public List<OrgResDto> findAll(Long companyId, boolean includeInactive) {

        List<Organization> orgs = includeInactive
                ? orgRepository.findByCompanyIdOrderByIsActiveDescParentOrgIdAscOrderIndexAsc(companyId)
                : orgRepository.findByCompanyIdAndIsActiveTrueOrderByParentOrgIdAscOrderIndexAsc(companyId);

        return orgs.stream()
                .map(OrgResDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrgAdminResDto> findAllForAdmin(Long companyId, boolean includeInactive) {

        List<Organization> orgs = includeInactive
                ? orgRepository.findByCompanyIdOrderByIsActiveDescParentOrgIdAscOrderIndexAsc(companyId)
                : orgRepository.findByCompanyIdAndIsActiveTrueOrderByParentOrgIdAscOrderIndexAsc(companyId);

        return orgs.stream()
                .map(o -> OrgAdminResDto.from(
                        o,
                        employeeRepository.countActiveEmployeesByOrg(companyId, o.getId()),
                        orgRepository.countByCompanyIdAndParentOrgIdAndIsActiveTrue(companyId, o.getId())
                ))
                .toList();
    }


    @Transactional(readOnly = true)
    public List<OrgResDto> search(
            Long companyId,
            String keyword,
            boolean includeInactive,
            boolean includeDescendants
    ) {
        String normalized = keyword.trim().toLowerCase();

        // 기준 데이터 로딩
        List<Organization> baseList = includeInactive
                ? orgRepository.findByCompanyIdOrderByIsActiveDescParentOrgIdAscOrderIndexAsc(companyId)
                : orgRepository.findByCompanyIdAndIsActiveTrueOrderByParentOrgIdAscOrderIndexAsc(companyId);

        // 빠른 탐색용 Map
        var orgMap = baseList.stream()
                .collect(java.util.stream.Collectors.toMap(
                        Organization::getId,
                        o -> o
                ));

        // 검색 히트 조직
        var matched = baseList.stream()
                .filter(o -> o.getName().toLowerCase().contains(normalized))
                .toList();

        java.util.Set<Long> resultIds = new java.util.HashSet<>();

        for (Organization org : matched) {

            // 2-1) 자기 자신
            resultIds.add(org.getId());

            // 2-2) 부모 체인 포함
            Long cursor = org.getParentOrgId();
            while (cursor != null) {
                Organization parent = orgMap.get(cursor);
                if (parent == null) break;
                resultIds.add(parent.getId());
                cursor = parent.getParentOrgId();
            }

            // 2-3) 하위 조직 포함 (옵션)
            if (includeDescendants) {
                collectDescendants(org.getId(), orgMap, resultIds);
            }
        }

        // 결과 DTO 변환
        return baseList.stream()
                .filter(o -> resultIds.contains(o.getId()))
                .map(OrgResDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrgAdminResDto> searchForAdmin(
            Long companyId,
            String keyword,
            boolean includeInactive,
            boolean includeDescendants
    ) {
        String normalized = keyword.trim().toLowerCase();

        // 기준 데이터 로딩
        List<Organization> baseList = includeInactive
                ? orgRepository.findByCompanyIdOrderByIsActiveDescParentOrgIdAscOrderIndexAsc(companyId)
                : orgRepository.findByCompanyIdAndIsActiveTrueOrderByParentOrgIdAscOrderIndexAsc(companyId);

        // 빠른 탐색용 Map
        var orgMap = baseList.stream()
                .collect(java.util.stream.Collectors.toMap(
                        Organization::getId,
                        o -> o
                ));

        // 검색 히트 조직
        var matched = baseList.stream()
                .filter(o -> o.getName().toLowerCase().contains(normalized))
                .toList();

        java.util.Set<Long> resultIds = new java.util.HashSet<>();

        for (Organization org : matched) {

            // 2-1) 자기 자신
            resultIds.add(org.getId());

            // 2-2) 부모 체인 포함
            Long cursor = org.getParentOrgId();
            while (cursor != null) {
                Organization parent = orgMap.get(cursor);
                if (parent == null) break;
                resultIds.add(parent.getId());
                cursor = parent.getParentOrgId();
            }

            // 2-3) 하위 조직 포함 (옵션)
            if (includeDescendants) {
                collectDescendants(org.getId(), orgMap, resultIds);
            }
        }

        // 결과 DTO 변환 (관리자용: 사원 수 + 하위 조직 수 포함)
        return baseList.stream()
                .filter(o -> resultIds.contains(o.getId()))
                .map(o -> OrgAdminResDto.from(
                        o,
                        employeeRepository.countActiveEmployeesByOrg(companyId, o.getId()),
                        orgRepository.countByCompanyIdAndParentOrgIdAndIsActiveTrue(companyId, o.getId())
                ))
                .toList();
    }

    /**
     * 하위 조직 재귀 수집
     */
    private void collectDescendants(
            Long parentId,
            java.util.Map<Long, Organization> orgMap,
            java.util.Set<Long> result
    ) {
        orgMap.values().stream()
                .filter(o -> parentId.equals(o.getParentOrgId()))
                .forEach(child -> {
                    if (result.add(child.getId())) {
                        collectDescendants(child.getId(), orgMap, result);
                    }
                });
    }


    /* ================= 수정 ================= */
    public void update(Long companyId, Long organizationId, OrgUpdateReqDto request) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalStateException("회사 정보 없음"));

        Organization org = getOrgInCompanyOrThrow(companyId, organizationId);

        String oldName = org.getName();
        String newName = normalizeNameOrThrow(request.getName());
        Boolean reqActive = request.getIsActive();

        // ===== 회사 루트 조직 =====
        if (org.getParentOrgId() == null) {

            if (!Objects.equals(oldName, newName)) {
                throw new InvalidStateException("회사명은 조직 관리에서 수정할 수 없습니다.");
            }

            if (Boolean.FALSE.equals(reqActive)) {
                throw new InvalidStateException("회사 루트 조직은 비활성화할 수 없습니다.");
            }

            return;
        }


        // ===== 일반 조직 =====

        // 비활성화
        if (Boolean.FALSE.equals(reqActive)) {
            deactivateInternal(companyId, org);
            return;
        }

        // 형제 단위 이름 중복
        if (orgRepository.existsByCompanyIdAndParentOrgIdAndNameAndIsActiveTrueAndIdNot(
                companyId, org.getParentOrgId(), newName, organizationId)) {
            throw new InvalidStateException("이미 존재하는 조직명입니다.");
        }

        // 재활성
        if (Boolean.TRUE.equals(reqActive) && Boolean.FALSE.equals(org.getIsActive())) {

            // 조직 카테고리 활성 여부 검증
            var category = orgCategoryRepository.findById(org.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("조직 카테고리를 찾을 수 없습니다."));

            if (!Boolean.TRUE.equals(category.getIsActive())) {
                throw new InvalidStateException(
                        "비활성 조직 카테고리에 속한 조직은 활성화할 수 없습니다."
                );
            }

            // 상위 조직 활성 여부 검증
            Organization parent = getOrgInCompanyOrThrow(companyId, org.getParentOrgId());
            if (!Boolean.TRUE.equals(parent.getIsActive())) {
                throw new InvalidStateException(
                        "상위 조직이 비활성 상태이므로 해당 조직을 활성화할 수 없습니다."
                );
            }

            // 재활성 처리
            int nextOrderIndex =
                    orgRepository.findMaxOrderIndex(companyId, org.getParentOrgId()) + 1;

            org.activate(nextOrderIndex);

            Employee actor = employeeRepository.findById(
                    SecurityUtils.getEmployeeId()
            ).orElseThrow(() -> new IllegalStateException("행위자 사원 정보 없음"));

            auditLogService.log(
                    company,
                    actor,
                    AuditEntityType.ORGANIZATION,
                    org.getId(),
                    AuditEventType.ACTIVATE,
                    Map.of("isActive", false),
                    Map.of("isActive", true)
            );
        }

        // 실제 값 반영
        org.update(org.getParentOrgId(), newName);
        orgRepository.flush();

        if (!Objects.equals(oldName, newName)) {
            organizationBoardCategorySyncService
                    .syncBoardName(companyId, organizationId, newName);
        }
    }

    /* ================= 비활성화 ================= */
    public void deactivate(Long companyId, Long organizationId) {
        Organization org = getOrgInCompanyOrThrow(companyId, organizationId);
        deactivateInternal(companyId, org);
    }


    /* ================= 정렬 ================= */
    @Transactional
    public void updateOrder(Long companyId, List<OrgOrderUpdateReqDto.OrderItem> orders) {

        if (orders == null || orders.isEmpty()) {
            throw new InvalidRequestException("순서 정보가 비어 있습니다.");
        }

        // 중복 ID 방지
        long distinctCount = orders.stream()
                .map(OrgOrderUpdateReqDto.OrderItem::getId)
                .distinct()
                .count();

        if (distinctCount != orders.size()) {
            throw new InvalidRequestException("중복된 조직 ID가 존재합니다.");
        }

        // 회사 소속 + 활성 조직 검증
        List<Organization> orgs = orders.stream()
                .map(o -> getOrgInCompanyOrThrow(companyId, o.getId()))
                .toList();

        Long parentOrgId = null;

        for (Organization org : orgs) {
            if (!Boolean.TRUE.equals(org.getIsActive())) {
                throw new InvalidStateException("비활성 조직은 정렬할 수 없습니다.");
            }

            if (parentOrgId == null) {
                parentOrgId = org.getParentOrgId();
            } else if (!Objects.equals(parentOrgId, org.getParentOrgId())) {
                throw new InvalidStateException("서로 다른 상위 조직은 함께 정렬할 수 없습니다.");
            }
        }


        // 임시 orderIndex (충돌 방지)
        int temp = -1;
        for (Organization org : orgs) {
            org.updateOrderIndex(temp--);
        }
        orgRepository.flush();

        // 최종 orderIndex 재할당 (프론트 순서 그대로)
        int orderIndex = 1;
        for (Organization org : orgs) {
            org.updateOrderIndex(orderIndex++);
        }
    }

    // 특정 조직 카테고리에 해당하는 조직들 조회
    @Transactional(readOnly = true)
    public List<OrgResDto> findByCategory(Long companyId, Long categoryId) {
        return orgRepository
                .findByCompanyIdAndCategoryIdAndIsActiveTrueOrderByOrderIndexAsc(companyId, categoryId)
                .stream()
                .map(OrgResDto::from)
                .toList();
    }


    /* ================= 내부 메서드들 ================= */
    private Organization getOrgInCompanyOrThrow(Long companyId, Long orgId) {
        return orgRepository.findByCompanyIdAndId(companyId, orgId)
                .orElseThrow(() -> new NotFoundException("조직을 찾을 수 없습니다."));
    }

    private Organization getActiveOrgInCompanyOrThrow(Long companyId, Long orgId) {
        Organization org = getOrgInCompanyOrThrow(companyId, orgId);
        if (!Boolean.TRUE.equals(org.getIsActive())) {
            throw new InvalidStateException("비활성 조직은 상위 조직으로 지정할 수 없습니다.");
        }
        return org;
    }

    private boolean isCycle(Long companyId, Long targetOrgId, Long newParentId) {
        Long cursor = newParentId;
        while (cursor != null) {
            if (Objects.equals(cursor, targetOrgId)) return true;
            cursor = getOrgInCompanyOrThrow(companyId, cursor).getParentOrgId();
        }
        return false;
    }

    private void validateCategoryActiveInCompany(Long companyId, Long categoryId) {
        // OrgCategory 엔티티 구조가 companyId를 갖고 있으니 이 방식으로 검증 가능
        var category = orgCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("조직 카테고리를 찾을 수 없습니다."));

        if (!Objects.equals(category.getCompanyId(), companyId)) {
            throw new ForbiddenException("해당 회사의 조직 카테고리가 아닙니다.");
        }
        if (!Boolean.TRUE.equals(category.getIsActive())) {
            throw new InvalidStateException("비활성 조직 카테고리는 사용할 수 없습니다.");
        }
    }

    private String normalizeNameOrThrow(String raw) {
        if (raw == null) throw new InvalidRequestException("조직명은 필수입니다.");
        String name = raw.trim();
        if (name.isBlank()) throw new InvalidRequestException("조직명은 필수입니다.");
        if (name.length() > 100) throw new InvalidRequestException("조직명은 100자 이하여야 합니다.");
        return name;
    }


    public List<OrgResDto> findOrgsByOrgId(Long orgId) {

        log.info("[OrgService] findHierarchy orgId={}", orgId);

        if (orgId == null) {
            log.error("[OrgService] orgId is null");
            return List.of();
        }

        List<OrgResView> hierarchy = orgRepository.findHierarchy(orgId);

        log.info("[OrgService] hierarchy size={}", hierarchy.size());

        return hierarchy.stream()
                .sorted(Comparator.comparing(OrgResView::getOrderIndex))
                .map(v -> new OrgResDto(
                        v.getId(),
                        v.getCategoryId(),
                        v.getParentOrgId(),
                        v.getName(),
                        v.getOrderIndex(),
                        v.getIsActive() != null && v.getIsActive() == 1
                ))
                .toList();
    }

    private void deactivateInternal(Long companyId, Organization org) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalStateException("회사 정보 없음"));


        // 루트 조직 비활성화 금지
        if (org.getParentOrgId() == null) {
            throw new InvalidStateException("회사 루트 조직은 비활성화할 수 없습니다.");
        }

        Long organizationId = org.getId();

        if (orgRepository.existsByCompanyIdAndParentOrgIdAndIsActiveTrue(companyId, organizationId)) {
            throw new InvalidStateException("하위 조직이 존재하여 비활성화할 수 없습니다.");
        }

        if (employeeRepository.existsActiveEmployeeInOrg(companyId, organizationId)) {
            throw new InvalidStateException("ACTIVE 사원이 존재하는 조직은 비활성화할 수 없습니다.");
        }

        log.info("[ORG DEACTIVATE] before org.deactivate() id={}", organizationId);
        org.deactivate();
        log.info("[ORG DEACTIVATE] after org.deactivate() id={}", organizationId);

        Employee actor = employeeRepository.findById(
                SecurityUtils.getEmployeeId()
        ).orElseThrow(() -> new IllegalStateException("행위자 사원 정보 없음"));

        auditLogService.log(
                company,
                actor,
                AuditEntityType.ORGANIZATION,
                org.getId(),
                AuditEventType.DEACTIVATE,
                Map.of("isActive", true),
                Map.of("isActive", false)
        );


        orgRepository.save(org);
        orgRepository.flush();
        orgPositionUsageRepository
                .deleteByCompany_IdAndOrganization_Id(companyId, organizationId);

        organizationBoardCategorySyncService.
                deactivateBoard(companyId, organizationId);
        log.info("[ORG DEACTIVATE] after deactivateBoard id={}", organizationId);

    }


    @Transactional(readOnly = true)
    public OrgDeactivateCheckResDto checkDeactivatable(Long companyId, Long orgId) {

        Organization org = getOrgInCompanyOrThrow(companyId, orgId);

        // 루트 조직
        if (org.getParentOrgId() == null) {
            return new OrgDeactivateCheckResDto(
                    false,
                    "회사 루트 조직은 비활성화할 수 없습니다."
            );
        }


        // 하위 조직 존재
        if (orgRepository.existsByCompanyIdAndParentOrgIdAndIsActiveTrue(companyId, orgId)) {
            return new OrgDeactivateCheckResDto(
                    false,
                    "하위 조직이 존재하여 비활성화할 수 없습니다."
            );
        }

        // 재직 중 사원 존재
        if (employeeRepository.existsActiveEmployeeInOrg(companyId, orgId)) {
            return new OrgDeactivateCheckResDto(
                    false,
                    "재직중인 사원이 존재하여 비활성화할 수 없습니다."
            );
        }

        return new OrgDeactivateCheckResDto(true, null);
    }


    // 사용자용 검색 메서드
    @Transactional(readOnly = true)
    public List<OrgResDto> searchForUser(
            Long companyId,
            String keyword,
            boolean includeDescendants
    ) {
        List<OrgResDto> result =
                search(companyId, keyword, false, includeDescendants); // 항상 활성만

        // 최상위 조직(회사명) 제외
        return result.stream()
                .filter(o -> o.getParentOrgId() != null)
                .toList();
    }

}
