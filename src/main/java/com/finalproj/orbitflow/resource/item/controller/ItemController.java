package com.finalproj.orbitflow.resource.item.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.resource.item.dto.ItemReqDto;
import com.finalproj.orbitflow.resource.item.dto.ItemResDto;
import com.finalproj.orbitflow.resource.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
 * @filename : ItemController
 * @since : 2025-12-17 오후 5:02 수요일
 */
@RequestMapping("/api")
@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // 관리자 - 기타 자원 리스트 조회
    @GetMapping("/admin/items")
    public ResponseEntity<ResponseDto> getItems(
            @AuthenticationPrincipal SecurityUser user,
            @PageableDefault(
                    page = 0,
                    size = 8,
                    sort = "id",
                    direction = Sort.Direction.ASC
            ) Pageable pageable
    ) {
        Long companyId = user.getCompanyId();

        Page<ItemResDto> items = itemService.getItems(companyId, pageable);

        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "자원 리스트 조회 성공", items)
        );
    }


    // 사용자 - 기타 자원 리스트 조회
    @GetMapping("items")
    public ResponseEntity<ResponseDto> getAvailableItems(@AuthenticationPrincipal SecurityUser user) {

        Long companyId = user.getCompanyId();
        List<ItemResDto> items = itemService.getAvailableItems(companyId);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "자원리스트 조회 성공", items)
        );
    }

    // 관리자 - 자원 카테고리별 조회
    @GetMapping("/admin/categories/{categoryId}/items")
    public ResponseEntity<ResponseDto> getItemsByCategory(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long categoryId,
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "id",
                    direction = Sort.Direction.ASC
            ) Pageable pageable
    ) {
        Long companyId = user.getCompanyId();
        Page<ItemResDto> items = itemService.getItemsByCategory(companyId, categoryId, pageable);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "카테고리 자원 리스트 조회 성공", items)
        );
    }

    // 사용자 - 자원 카테고리별 조회
    @GetMapping("/categories/{categoryId}/items")
    public ResponseEntity<ResponseDto> getAvailableItemsByCategory(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long categoryId
    ) {
        Long companyId = user.getCompanyId();
        List<ItemResDto> items = itemService.getItemsByStatusAndCategory(companyId, categoryId);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "카테고리 자원 리스트 조회 성공", items)
        );
    }

    // 관리자 | 사용자 - 자원 상세 조회
    @GetMapping("/items/{itemId}")
    public ResponseEntity<ResponseDto> getItem(
            @PathVariable Long itemId
    ) {
        ItemResDto item = itemService.getItem(itemId);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "자원 조회 성공", item)
        );
    }

    @PostMapping("/admin/items")
    public ResponseEntity<ResponseDto> insertItem(
            @AuthenticationPrincipal SecurityUser user,
            @ModelAttribute ItemReqDto dto
    ) {
        Long companyId = user.getCompanyId();
        Long employeeId = user.getEmployeeId();
        itemService.insertItem(companyId, employeeId, dto);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "자원 등록 성공", null)
        );
    }

    // 관리자 - 자원 수정
    @PutMapping("/admin/items/{itemId}")
    public ResponseEntity<ResponseDto> updateItem(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long itemId,
            @ModelAttribute ItemReqDto dto
    ) {
        Long companyId = user.getCompanyId();
        Long employeeId = user.getEmployeeId();

        itemService.updateItem(companyId, employeeId, itemId, dto);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "자원 수정 성공", null)
        );
    }

    // 관리자 - 자원 삭제
    @PatchMapping("/admin/items/{itemId}/delete")
    public ResponseEntity<ResponseDto> deleteItem(
            @PathVariable Long itemId
    ) {
        itemService.deleteItem(itemId);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "자원 삭제 성공", null)
        );
    }

}
