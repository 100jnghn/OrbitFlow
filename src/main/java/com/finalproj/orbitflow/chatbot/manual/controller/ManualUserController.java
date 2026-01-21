package com.finalproj.orbitflow.chatbot.manual.controller;

import com.finalproj.orbitflow.chatbot.manual.service.ManualUploadService;
import com.finalproj.orbitflow.chatbot.manualcategory.dto.ManualCategoryResDto;
import com.finalproj.orbitflow.chatbot.manualcategory.entity.ManualCategory;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자용 매뉴얼 카테고리 조회 Controller
 *
 * @author : rlagkdus
 * @filename : ManualUserController
 * @since : 2025. 12. 30. 화요일
 */
@RestController
@RequestMapping("/api/manual")
@RequiredArgsConstructor
public class ManualUserController {

    private final ManualUploadService manualUploadService;

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
}

