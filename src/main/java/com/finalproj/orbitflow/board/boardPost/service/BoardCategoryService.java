package com.finalproj.orbitflow.board.boardPost.service;

import com.finalproj.orbitflow.board.boardCategory.dto.BoardCategoryResDto;
import com.finalproj.orbitflow.board.boardCategory.repository.BoardCategoryRepository;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeRole;
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
     * [사용자용] 본인 소속 조직 게시판 목록 조회 (활성화된 게시판만)
     */
    public List<BoardCategoryResDto.Category> getOrganizationBoards(Long orgId) {
        return boardCategoryRepository
                .findByOrganization_IdAndIsActivatedTrueAndDeletedAtIsNull(orgId)
                .stream()
                .map(BoardCategoryResDto.Category::from)
                .toList();
    }
}


