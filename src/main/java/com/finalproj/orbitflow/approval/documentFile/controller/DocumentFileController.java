package com.finalproj.orbitflow.approval.documentFile.controller;

import com.finalproj.orbitflow.approval.documentFile.dto.DocumentFileAttachedListResDto;
import com.finalproj.orbitflow.approval.documentFile.dto.DocumentFileUploadResDto;
import com.finalproj.orbitflow.approval.documentFile.enums.DocumentFileStatus;
import com.finalproj.orbitflow.approval.documentFile.service.DocumentFileService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentFileController
 * @since : 26. 1. 2. 금요일
 **/

@RestController
@RequestMapping("/api/document-file")
@RequiredArgsConstructor
public class DocumentFileController {

    private final DocumentFileService documentFileService;

    @PostMapping("/{documentId}")
    public ResponseEntity<ResponseDto> upload(
            @PathVariable Long documentId,
            @RequestParam MultipartFile file
    ) {

        DocumentFileUploadResDto result = documentFileService.uploadDocumentFile(
                        SecurityUtils.getCompanyId(),
                        SecurityUtils.getEmployeeId(),
                        documentId,
                        file
                );

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.CREATED, "파일 업로드 성공",  result));
    }

    @PostMapping("/{documentId}/image")
    public ResponseEntity<ResponseDto> uploadImage(
            @PathVariable Long documentId,
            @RequestParam String fieldId,
            @RequestParam MultipartFile file
    ) {
        DocumentFileUploadResDto result =
                documentFileService.uploadImageFile(
                        SecurityUtils.getCompanyId(),
                        SecurityUtils.getEmployeeId(),
                        documentId,
                        fieldId,
                        file
                );

        return ResponseEntity.ok(
                new ResponseDto<>(HttpStatus.CREATED, "이미지 업로드 성공", result)
        );
    }

    @GetMapping("/{documentId}/files")
    public ResponseEntity<ResponseDto> getAttachedFiles(
            @PathVariable Long documentId
    ) {
        List<DocumentFileAttachedListResDto> result = documentFileService.getAttachedFiles(SecurityUtils.getEmployeeId(), documentId);

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "첨부 파일 목록 조회 성공",  result));
    }


    @PatchMapping("/{documentFileId}/status")
    public ResponseEntity<ResponseDto> updateStatus(
            @PathVariable Long documentFileId,
            @RequestParam DocumentFileStatus status
    ) {
        documentFileService.updateStatus(SecurityUtils.getEmployeeId(), documentFileId, status);

        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "첨부 파일 상태 수정 성공", null));
    }


    @GetMapping("/{documentId}/images/{fileId}")
    public ResponseEntity<byte[]> getDocumentImage(
            @PathVariable Long documentId,
            @PathVariable Long fileId
    ) {
        return documentFileService.getDocumentImage(
                SecurityUtils.getEmployeeId(),
                documentId,
                fileId
        );
    }
}
