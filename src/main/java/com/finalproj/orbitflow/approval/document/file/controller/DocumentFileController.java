package com.finalproj.orbitflow.approval.document.file.controller;

import com.finalproj.orbitflow.approval.document.file.dto.DocumentFileAttachedListResDto;
import com.finalproj.orbitflow.approval.document.file.dto.DocumentFileUploadResDto;
import com.finalproj.orbitflow.approval.document.file.dto.PdfStatusRes;
import com.finalproj.orbitflow.approval.document.file.enums.DocumentFileStatus;
import com.finalproj.orbitflow.approval.document.file.service.DocumentFileService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 결재 문서에 첨부되는 파일 및 이미지 업로드와 조회를 처리하는 컨트롤러.
 * <p>
 * 문서에 대한 일반 첨부파일, 문서 본문에 포함되는 이미지 파일 업로드를 지원하며,
 * 업로드된 파일의 상태 변경, 첨부 목록 조회, 이미지 조회 기능을 제공한다.
 * <p>
 * PDF 변환 상태 조회와 같이 문서 파일 처리와 관련된 부가 기능도 함께 제공하며,
 * 실제 파일 저장 및 권한 검증 로직은 DocumentFileService에 위임한다.
 * <p>
 * 컨트롤러는 요청 수신과 응답 반환만 담당하고,
 * 파일 처리에 대한 상세 로직은 서비스 레이어에서 관리한다.
 *
 * @author : Choi MinHyeok
 * @filename : DocumentFileController
 * @since : 26. 1. 2. 금요일
 */


@RestController
@RequestMapping("/api/document-file")
@RequiredArgsConstructor
public class DocumentFileController {

    private final DocumentFileService documentFileService;

    @PostMapping("/{documentId}")
    public ResponseEntity<?> upload(
            @PathVariable Long documentId,
            @RequestParam MultipartFile file
    ) {

        DocumentFileUploadResDto result = documentFileService.uploadDocumentFile(
                SecurityUtils.getCompanyId(),
                SecurityUtils.getEmployeeId(),
                documentId,
                file
        );

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.CREATED, "파일 업로드 성공", result));
    }

    @PostMapping("/{documentId}/image")
    public ResponseEntity<?> uploadImage(
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
    public ResponseEntity<?> getAttachedFiles(
            @PathVariable Long documentId
    ) {
        List<DocumentFileAttachedListResDto> result = documentFileService.getAttachedFiles(SecurityUtils.getEmployeeId(), documentId);

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "첨부 파일 목록 조회 성공", result));
    }


    @PatchMapping("/{documentFileId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long documentFileId,
            @RequestParam DocumentFileStatus status
    ) {
        documentFileService.updateStatus(SecurityUtils.getEmployeeId(), documentFileId, status);

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "첨부 파일 상태 수정 성공", null));
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

    @GetMapping("/{documentId}/pdf")
    public ResponseEntity<?> getDocumentPdfStatus(
            @PathVariable Long documentId
    ) {

        PdfStatusRes result = documentFileService.getPdfStatus(documentId);

        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "PDF 문서 상태 조회 성공",
                        result
                )
        );
    }
}
