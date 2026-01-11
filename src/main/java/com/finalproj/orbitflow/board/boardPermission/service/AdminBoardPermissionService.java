package com.finalproj.orbitflow.board.boardPermission.service;

import com.finalproj.orbitflow.board.boardCategory.entity.BoardCategory;
import com.finalproj.orbitflow.board.boardCategory.repository.BoardCategoryRepository;
import com.finalproj.orbitflow.board.boardPermission.dto.BoardPermissionResDto;
import com.finalproj.orbitflow.board.boardPermission.entity.BoardPermission;
import com.finalproj.orbitflow.board.boardPermission.repository.BoardPermissionRepository;
import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 관리자 게시판 권한 관리 Service
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminBoardPermissionService {

    private final BoardPermissionRepository boardPermissionRepository;
    private final BoardCategoryRepository boardCategoryRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * [관리자용] 특정 게시판에 권한이 부여된 직원 목록 조회
     */
    public List<BoardPermissionResDto.Permission> getBoardPermissions(Long companyId, Long boardCategoryId) {
        // 게시판 존재 및 회사 소유 확인
        BoardCategory category = boardCategoryRepository.findById(boardCategoryId)
                .orElseThrow(() -> new NotFoundException("게시판을 찾을 수 없습니다."));

        if (!category.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException("해당 게시판에 대한 권한이 없습니다.");
        }

        List<BoardPermission> permissions = boardPermissionRepository.findAllByBoardCategory_Id(boardCategoryId);
        
        // Employee의 연관관계(organization, rank)를 로드하기 위해 접근
        permissions.forEach(permission -> {
            if (permission.getEmployee().getOrganization() != null) {
                permission.getEmployee().getOrganization().getName();
            }
            if (permission.getEmployee().getRank() != null) {
                permission.getEmployee().getRank().getName();
            }
        });
        
        return BoardPermissionResDto.Permission.fromList(permissions);
    }

    /**
     * [관리자용] 게시판 권한 제거
     */
    @Transactional
    public void removeBoardPermission(Long companyId, Long permissionId) {
        BoardPermission permission = boardPermissionRepository.findById(permissionId)
                .orElseThrow(() -> new NotFoundException("권한을 찾을 수 없습니다."));

        // 권한이 속한 게시판의 회사 확인
        BoardCategory category = permission.getBoardCategory();
        if (!category.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException("해당 권한에 대한 접근 권한이 없습니다.");
        }

        boardPermissionRepository.delete(permission);
    }

    /**
     * [관리자용] 게시판 권한 부여
     */
    @Transactional
    public List<BoardPermissionResDto.Permission> grantBoardPermissions(
            Long companyId, 
            Long boardCategoryId, 
            List<Long> employeeIds
    ) {
        BoardCategory category = boardCategoryRepository.findById(boardCategoryId)
                .orElseThrow(() -> new NotFoundException("게시판을 찾을 수 없습니다."));

        if (!category.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException("해당 게시판에 대한 권한이 없습니다.");
        }

        // employeeIds -> Employee 엔티티 조회
        List<Employee> employees = employeeRepository.findAllById(employeeIds);
        if (employees.size() != employeeIds.size()) {
            throw new NotFoundException("존재하지 않는 직원이 포함되어 있습니다.");
        }

        // 회사 검증(중요): 다른 회사 직원에게 권한 부여 방지
        boolean hasOtherCompany = employees.stream()
                .anyMatch(e -> !e.getCompany().getId().equals(companyId));
        if (hasOtherCompany) {
            throw new ForbiddenException("다른 회사 직원에게는 권한을 부여할 수 없습니다.");
        }

        // 이미 권한이 있는 직원은 제외하고 새로 권한 부여
        List<BoardPermission> toSave = employees.stream()
                .filter(e -> boardPermissionRepository
                        .findByEmployee_IdAndBoardCategory_Id(e.getId(), boardCategoryId)
                        .isEmpty()
                )
                .map(e -> BoardPermission.builder()
                        .employee(e)
                        .boardCategory(category)
                        .build()
                )
                .toList();

        List<BoardPermission> saved = boardPermissionRepository.saveAll(toSave);
        
        // Employee의 연관관계(organization, rank)를 로드하기 위해 접근
        saved.forEach(permission -> {
            if (permission.getEmployee().getOrganization() != null) {
                permission.getEmployee().getOrganization().getName();
            }
            if (permission.getEmployee().getRank() != null) {
                permission.getEmployee().getRank().getName();
            }
        });
        
        return BoardPermissionResDto.Permission.fromList(saved);
    }
}

