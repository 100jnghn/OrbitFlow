package com.finalproj.orbitflow.board.controller.admin;

import com.finalproj.orbitflow.board.dto.admin.AdminBoardResDto;
import com.finalproj.orbitflow.board.service.admin.AdminBoardCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public Page<AdminBoardResDto.Category> getCategoryList(
            @RequestParam Long companyId,
            @PageableDefault(size = 5, sort = "createdAt") Pageable pageable
    ) {
        return adminBoardCategoryService.getCategoryList(companyId, pageable);
    }
}
