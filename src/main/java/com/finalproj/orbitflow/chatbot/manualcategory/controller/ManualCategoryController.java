package com.finalproj.orbitflow.chatbot.manualcategory.controller;


import com.finalproj.orbitflow.chatbot.manualcategory.dto.ManualCategoryReqDto;
import com.finalproj.orbitflow.chatbot.manualcategory.service.ManualCategoryService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author : rlagkdus
 * @filename : ManualCategoryController
 * @since : 2025. 12. 30. 화요일
 */

@RestController
@RequestMapping("/api/admin/manual/categories")
@RequiredArgsConstructor
public class ManualCategoryController {

    private final ManualCategoryService categoryService;

    @PostMapping
    public ResponseEntity<ResponseDto> createCategory(
            @AuthenticationPrincipal SecurityUser user,
            @RequestBody ManualCategoryReqDto requestDto) {
        categoryService.createCategory(user.getCompanyId(), requestDto);
        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "카테고리가 생성되었습니다.", null));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<ResponseDto> updateCategory(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long categoryId,
            @RequestBody ManualCategoryReqDto requestDto) {
        categoryService.updateCategory(user.getCompanyId(), categoryId, requestDto);
        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "카테고리가 수정되었습니다.", null));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ResponseDto> deleteCategory(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long categoryId) {
        categoryService.deleteCategory(user.getCompanyId(), categoryId);
        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "카테고리가 삭제되었습니다.", null));
    }
}
