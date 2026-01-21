package com.finalproj.orbitflow.board.boardpost.service;

import com.finalproj.orbitflow.board.boardcategory.dto.BoardCategoryResDto;
import com.finalproj.orbitflow.board.boardcategory.repository.BoardCategoryRepository;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeRole;
import com.finalproj.orbitflow.hr.organization.repository.OrgRepository;
import com.finalproj.orbitflow.hr.organization.repository.OrgResView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사용자 게시판 카테고리 Service
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardCategoryService {

    private final BoardCategoryRepository boardCategoryRepository;
    private final OrgRepository orgRepository;

    /**
     * [사용자용] 권한이 부여된 활성 일반 게시판 목록 조회
     * - ADMIN 권한이 있으면 모든 활성화된 공용 게시판 조회
     * - 일반 직원: 권한(permissions)이 하나도 없는 일반 게시판 = 전 직원에게 노출(공용)
     * - 일반 직원: 권한(permissions)이 존재하는 일반 게시판 = 권한 있는 직원에게만 노출(제한)
     */
    public List<BoardCategoryResDto.Category> getAccessibleBoards(Long companyId, Long employeeId, EmployeeRole role) {
        // ADMIN 권한이 있으면 모든 활성화된 공용 게시판 조회
        if (role == EmployeeRole.ADMIN || role == EmployeeRole.COMPANY_ADMIN) {
            return boardCategoryRepository
                    .findByCompany_IdAndOrganizationIsNullAndIsActivatedTrueAndDeletedAtIsNull(companyId)
                    .stream()
                    .map(BoardCategoryResDto.Category::from)
                    .toList();
        }

        // 일반 직원: 권한이 있는 게시판만 조회
        return boardCategoryRepository
                .findAccessiblePublicBoards(companyId, employeeId)
                .stream()
                .map(BoardCategoryResDto.Category::from)
                .toList();
    }

    /**
     * [사용자용] 본인 소속 조직 및 상위 조직 게시판 목록 조회 (활성화된 게시판만)
     * - 최상위 회사(parentOrgId == null)는 제외
     */
    public List<BoardCategoryResDto.Category> getOrganizationBoards(Long orgId) {
        // 1. 해당 조직의 상위 계층 구조 조회 (CTE 사용)
        List<OrgResView> hierarchy = orgRepository.findHierarchy(orgId);

        // 2. 최상위 회사(parentOrgId 가 null 인 항목) 제외하고 ID 추출
        List<Long> orgIds = new java.util.ArrayList<>(hierarchy.stream()
                .filter(view -> view.getParentOrgId() != null)
                .map(OrgResView::getId)
                .toList());
        java.util.Collections.reverse(orgIds);

        if (orgIds.isEmpty()) {
            return List.of();
        }

        // 3. 해당 조직들의 게시판 조회
        // Repository에서 In 절을 사용하므로 순서가 보장되지 않을 수 있음
        List<BoardCategoryResDto.Category> boards = boardCategoryRepository
                .findByOrganization_IdInAndIsActivatedTrueAndDeletedAtIsNull(orgIds)
                .stream()
                .map(BoardCategoryResDto.Category::from)
                .toList();

        // 4. 계층 구조 순서(보통 하위->상위 또는 상위->하위)에 맞게 정렬하면 좋음
        // 여기서는 hierarchy에 나온 순서대로 정렬 (hierarchy는 현재 조직부터 상위로 올라감)
        return orgIds.stream()
                .flatMap(id -> boards.stream()
                        .filter(b -> b.getOrganizationId() != null && b.getOrganizationId().equals(id)))
                .toList();
    }
}
