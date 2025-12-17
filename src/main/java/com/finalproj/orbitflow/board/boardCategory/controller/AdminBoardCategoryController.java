package com.finalproj.orbitflow.board.boardCategory.controller;

import com.finalproj.orbitflow.board.boardCategory.dto.BoardCategoryResDto;
import com.finalproj.orbitflow.board.boardCategory.service.AdminBoardCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/board-categories")
@RequiredArgsConstructor
public class AdminBoardCategoryController {

    private final AdminBoardCategoryService adminBoardCategoryService;

    /**
     * 관리자 게시판(카테고리) 목록 조회
     *
     * @param companyId 회사 ID
     * @param pageable  페이징 정보
     */
    @GetMapping
    public Page<BoardCategoryResDto.Category> getCategoryList(
            @RequestParam Long companyId,
            @PageableDefault(size = 5, sort = "createdAt") Pageable pageable
    ) {
        return adminBoardCategoryService.getCategoryList(companyId, pageable);
    }

    /**
     * 관리자 게시판 카테고리 단건 조회
     * - 수정 페이지 로딩용
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<BoardCategoryResDto.Category> getCategoryDetail(
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(
                adminBoardCategoryService.getCategoryDetail(categoryId)
        );
    }
}
