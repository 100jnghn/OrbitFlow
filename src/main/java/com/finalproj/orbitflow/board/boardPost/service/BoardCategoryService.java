package com.finalproj.orbitflow.board.boardPost.service;

import com.finalproj.orbitflow.board.boardCategory.dto.BoardCategoryResDto;
import com.finalproj.orbitflow.board.boardCategory.repository.BoardCategoryRepository;
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
     */
    public List<BoardCategoryResDto.Category> getAccessibleBoards(Long employeeId) {
        return boardCategoryRepository
                .findByBoardPermissions_Employee_IdAndIsActivatedTrueAndDeletedAtIsNull(employeeId)
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


