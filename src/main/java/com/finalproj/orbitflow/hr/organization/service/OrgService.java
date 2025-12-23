package com.finalproj.orbitflow.hr.organization.service;

import com.finalproj.orbitflow.board.boardCategory.service.OrganizationBoardCategorySyncService;
import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.InvalidStateException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.hr.orgCategory.repository.OrgCategoryRepository;
import com.finalproj.orbitflow.hr.orgPositionUsage.repository.OrgPositionUsageRepository;
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
    private final OrgCategoryRepository orgCategoryRepository;
    private final OrgPositionUsageRepository orgPositionUsageRepository;
    private final OrganizationBoardCategorySyncService organizationBoardCategorySyncService;

    /* ================= 생성 ================= */
    public Long create(Long companyId, OrgCreateReqDto request) {

        Long parentOrgId = request.getParentOrgId();
        Long categoryId = request.getCategoryId();
        String name = normalizeNameOrThrow(request.getName());

        // 카테고리 검증(존재 + 같은 회사 + 활성)
        validateCategoryActiveInCompany(companyId, categoryId);

        // 상위 조직 검증(있다면 존재 + 같은 회사 + 활성)
        if (parentOrgId != null) {
            Organization parent = getActiveOrgInCompanyOrThrow(companyId, parentOrgId);
            // parent 변수는 검증용 (미사용이어도 OK)
        }

        // 형제 단위 이름 중복(활성 기준)
        if (orgRepository.existsByCompanyIdAndParentOrgIdAndNameAndIsActiveTrue(companyId, parentOrgId, name)) {
            throw new InvalidStateException("이미 존재하는 조직명입니다.");
        }

        // 다음 orderIndex (형제 단위)
        int nextOrderIndex = (int) orgRepository.countByCompanyIdAndParentOrgIdAndIsActiveTrue(companyId, parentOrgId) + 1;

        Organization org = Organization.create(companyId, categoryId, parentOrgId, name, nextOrderIndex);
        Organization saved = orgRepository.save(org);

        // 조직 게시판 카테고리 자동 생성
        organizationBoardCategorySyncService.createIfAbsent(companyId, saved.getId(), saved.getName());

        return saved.getId();
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
    public void update(Long companyId, Long organizationId, OrgUpdateReqDto request) {

        Organization org = getOrgInCompanyOrThrow(companyId, organizationId);
        String oldName = org.getName();

        Long newParentId = request.getParentOrgId();
        String newName = normalizeNameOrThrow(request.getName());

        // 자기 자신을 상위로 금지
        if (newParentId != null && Objects.equals(newParentId, organizationId)) {
            throw new InvalidRequestException("자기 자신을 상위 조직으로 지정할 수 없습니다.");
        }

        // 상위 조직 검증(존재/회사/활성)
        if (newParentId != null) {
            getActiveOrgInCompanyOrThrow(companyId, newParentId);
        }

        // 순환 참조 방지: newParent의 조상 중에 organizationId가 있으면 금지
        if (newParentId != null && isCycle(companyId, organizationId, newParentId)) {
            throw new InvalidStateException("조직 트리에 순환 구조가 발생할 수 있어 상위 조직을 변경할 수 없습니다.");
        }

        // 형제 단위 이름 중복 (수정: 자기 자신 제외)
        if (orgRepository.existsByCompanyIdAndParentOrgIdAndNameAndIsActiveTrueAndIdNot(
                companyId, newParentId, newName, organizationId)) {
            throw new InvalidStateException("이미 존재하는 조직명입니다.");
        }

        // 부모가 바뀌면: 새 부모의 마지막 순번으로 이동
        boolean parentChanged = !Objects.equals(org.getParentOrgId(), newParentId);
        if (parentChanged) {
            int nextOrderIndex = (int) orgRepository.countByCompanyIdAndParentOrgIdAndIsActiveTrue(companyId, newParentId) + 1;
            org.updateOrderIndex(nextOrderIndex);
        }

        org.update(newParentId, newName); // categoryId는 변경 불가이므로 기존 값 유지

        // 이름이 바뀐 경우에만 게시판 카테고리명 동기화 (정책 선택)
        if (!oldName.equals(newName)) {
            organizationBoardCategorySyncService.syncBoardName(companyId, organizationId, newName);
        }
    }

    /* ================= 비활성화 ================= */
    public void deactivate(Long companyId, Long organizationId) {

        Organization org = getOrgInCompanyOrThrow(companyId, organizationId);

        // 하위 조직 존재하면 비활성화 불가(트리 무결성)
        if (orgRepository.existsByCompanyIdAndParentOrgIdAndIsActiveTrue(companyId, organizationId)) {
            throw new InvalidStateException("하위 조직이 존재하여 비활성화할 수 없습니다.");
        }

        // 기존 정책: 조직에 재직/정지/임시 계정이 남아 있으면 비활성화 불가
        if (employeeRepository.existsByCompanyIdAndOrganizationIdAndStatusNot(
                companyId,
                organizationId,
                EmployeeStatus.RESIGNED
        )) {
            throw new InvalidStateException("해당 조직에 소속된 재직 중인 사원이 존재하여 비활성화할 수 없습니다.");
        }

        orgPositionUsageRepository.deleteByCompany_IdAndOrganization_Id(companyId, organizationId);

        org.deactivate();

        // 조직 게시판도 비활성화(삭제 대신)
        organizationBoardCategorySyncService.deactivateBoard(companyId, organizationId);
    }



    /* ================= 정렬 ================= */
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

        // 같은 parentOrgId인지 검증 + 활성 검증
        Long parentOrgId = null;

        for (OrgOrderUpdateReqDto.OrderItem item : orders) {
            Organization org = getOrgInCompanyOrThrow(companyId, item.getId());

            if (!Boolean.TRUE.equals(org.getIsActive())) {
                throw new InvalidStateException("비활성 조직은 정렬 대상이 될 수 없습니다.");
            }

            if (parentOrgId == null) {
                parentOrgId = org.getParentOrgId();
            } else if (!Objects.equals(parentOrgId, org.getParentOrgId())) {
                throw new InvalidStateException("서로 다른 상위 조직의 조직은 함께 정렬할 수 없습니다.");
            }
        }

        // 정렬 대상 개수 검증(형제 단위)
        long activeCount = orgRepository.countByCompanyIdAndParentOrgIdAndIsActiveTrue(companyId, parentOrgId);
        if (activeCount != orders.size()) {
            throw new InvalidStateException("정렬 대상 개수가 일치하지 않습니다.");
        }

        // 임시 orderIndex 할당 (충돌 방지)
        int tempIndex = -1;
        for (OrgOrderUpdateReqDto.OrderItem item : orders) {
            Organization org = getOrgInCompanyOrThrow(companyId, item.getId());
            org.updateOrderIndex(tempIndex--);
        }

        orgRepository.flush();

        // 최종 orderIndex 재할당
        int orderIndex = 1;
        for (OrgOrderUpdateReqDto.OrderItem item : orders) {
            Organization org = getOrgInCompanyOrThrow(companyId, item.getId());
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
            if (Objects.equals(cursor, targetOrgId)) {
                return true;
            }
            Organization parent = getOrgInCompanyOrThrow(companyId, cursor);
            cursor = parent.getParentOrgId();
        }
        return false;
    }

    private void validateCategoryActiveInCompany(Long companyId, Long categoryId) {
        // OrgCategory 엔티티 구조가 companyId를 갖고 있으니 이 방식으로 검증 가능
        com.finalproj.orbitflow.hr.orgCategory.entity.OrgCategory category =
                orgCategoryRepository.findById(categoryId)
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
}
