package com.finalproj.orbitflow.resource.itemcategory.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.resource.itemcategory.dto.ItemCategoryDto;
import com.finalproj.orbitflow.resource.itemcategory.service.ItemCategoryService;
import com.finalproj.orbitflow.resource.itemcategory.tempexception.ConfirmRequiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ItemCategoryController
 * @since : 2025-12-17 오후 2:51 수요일
 */
@RequestMapping("/api")
@Controller
@RequiredArgsConstructor
public class ItemCategoryController {

    private final ItemCategoryService itemCategoryService;

    // 기타 자원 카테고리 목록 조회
    @GetMapping("/admin/item-categories")
    public ResponseEntity<ResponseDto> getItemCategories(
            @AuthenticationPrincipal SecurityUser user
    ) {
        Long companyId = user.getCompanyId();
        List<ItemCategoryDto> itemCategories = itemCategoryService.getItemCategories(companyId);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "자원 목록 조회 성공", itemCategories)
        );
    }

    // 기타 자원 카테고리 상세 조회
    @GetMapping("/admin/item-categories/{itemCategoryId}")
    public ResponseEntity<ResponseDto> getItemCategory(
            @PathVariable Long itemCategoryId
    ) {
        ItemCategoryDto dto = itemCategoryService.getItemCategory(itemCategoryId);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "자원 카테고리 상세 조회 성공", dto)
        );
    }

    // 기타 자원 카테고리 정보 수정
    @PutMapping("/admin/item-categories/{itemCategoryId}")
    public ResponseEntity<ResponseDto> updateItemCategory(
            @PathVariable Long itemCategoryId,
            @RequestBody ItemCategoryDto dto
    ) {
        itemCategoryService.updateItemCategory(itemCategoryId, dto);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "자원 카테고리 수정 성공", null)
        );
    }

    // 기타 자원 카테고리 삭제
    @DeleteMapping("/admin/item-categories/{itemCategoryId}")
    public ResponseEntity<ResponseDto> deleteItemCategory(
            @PathVariable Long itemCategoryId,
            @RequestParam(value = "force", defaultValue = "false") boolean force
    ) {

        try {
            itemCategoryService.deleteItemCategory(itemCategoryId, force);

            return ResponseEntity.ok().body(
                    new ResponseDto(HttpStatus.OK, "카테고리 및 하위 자원 삭제 완료", null)
            );
        } catch (ConfirmRequiredException e) {
            // 하위 자원 삭제 경고가 필요한 경우 -> 409 error
            // 프론트에서 confirm 창을 띄우고 확인 -> force = true로 재요청
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new ResponseDto(HttpStatus.CONFLICT, e.getMessage(), "확인이 필요합니다.")
            );
        }
    }
}
