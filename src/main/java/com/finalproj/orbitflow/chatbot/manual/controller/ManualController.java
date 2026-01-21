package com.finalproj.orbitflow.chatbot.manual.controller;

import com.finalproj.orbitflow.chatbot.manual.entity.ManualMetadata;
import com.finalproj.orbitflow.chatbot.manual.service.ManualUploadService;
import com.finalproj.orbitflow.chatbot.manualCategory.dto.ManualCategoryResDto;
import com.finalproj.orbitflow.chatbot.manualCategory.entity.ManualCategory;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ManualController
 * @since : 2025. 12. 30. 화요일
 */

@RestController
@RequestMapping("/api/admin/manual")
@RequiredArgsConstructor
public class ManualController {

    private final ManualUploadService manualUploadService;

    @PostMapping("/upload")
    public ResponseEntity<ResponseDto> uploadManual(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam("file") MultipartFile file,
            @RequestParam("categoryId") Long categoryId) {

        manualUploadService.uploadAndIndexingManual(file, categoryId, user.getEmployeeId());

        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "매뉴얼 업로드 및 학습 완료", null));
    }


    @GetMapping("/list")
    public ResponseEntity<ResponseDto> getManualList(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) Long categoryId) {
        // 카테고리 ID가 제공된 경우 해당 카테고리의 매뉴얼만 조회, 없으면 전체 조회
        List<ManualMetadata> list;
        if (categoryId != null) {
            list = manualUploadService.findByCompanyAndCategory(user.getCompanyId(), categoryId);
        } else {
            list = manualUploadService.findAllByCompany(user.getCompanyId());
        }

        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "매뉴얼 목록 조회 완료", list));
    }

    @GetMapping("/categories")
    public ResponseEntity<ResponseDto<List<ManualCategoryResDto>>> getCategories(
            @AuthenticationPrincipal SecurityUser user) {

        List<ManualCategory> categories = manualUploadService.findActiveCategoriesByCompany(user.getCompanyId());
        
        List<ManualCategoryResDto> categoryDtos = categories.stream()
                .map(category -> ManualCategoryResDto.builder()
                        .id(category.getId())
                        .categoryName(category.getCategoryName())
                        .description(category.getDescription())
                        .sortOrder(category.getSortOrder())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "카테고리 목록 조회 완료", categoryDtos));
    }

    @DeleteMapping("/{manualId}")
    public ResponseEntity<ResponseDto> deleteManual(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long manualId) {
        manualUploadService.deleteManual(manualId, user.getCompanyId());
        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "매뉴얼 삭제 완료", null));
    }


    @PatchMapping("/{manualId}/active")
    public ResponseEntity<ResponseDto> updateManualActive(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long manualId,
            @RequestParam boolean isActive
    ) {
        manualUploadService.updateManualActive(
                user.getCompanyId(),
                manualId,
                isActive
        );

        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "매뉴얼 활성 상태 변경 완료", null)
        );
    }



}
