package com.finalproj.orbitflow.chatbot.manual.controller;

import com.finalproj.orbitflow.chatbot.manual.service.ManualUploadService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
}
