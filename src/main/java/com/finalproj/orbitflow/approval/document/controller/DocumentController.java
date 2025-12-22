package com.finalproj.orbitflow.approval.document.controller;

import com.finalproj.orbitflow.approval.document.dto.DocumentCreateResDto;
import com.finalproj.orbitflow.approval.document.dto.DocumentListReqDto;
import com.finalproj.orbitflow.approval.document.dto.DocumentListResDto;
import com.finalproj.orbitflow.approval.document.service.DocumentService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentController
 * @since : 25. 12. 22. 월요일
 **/

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
public class DocumentController {
    private final DocumentService documentService;


    @GetMapping
    public ResponseEntity<ResponseDto> getMyDocuments(
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "10") int size,
            DocumentListReqDto reqDto
    ) {

        Page<DocumentListResDto> result = documentService.getMyDocuments(SecurityUtils.getCompanyId(), SecurityUtils.getEmployeeId(), offset, size, reqDto);

        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "나의 기안 목록 조회 성공", result));
    }


    @PostMapping("/draft/{formTemplateId}")
    public ResponseEntity<ResponseDto> createDocument(
        @PathVariable Long formTemplateId,
        @RequestParam String documentTitle
    ) {
        DocumentCreateResDto result = documentService.createDraft(SecurityUtils.getCompanyId(), formTemplateId, documentTitle);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.CREATED, "결재 문서 초안 생성 성공", result));
    }
}
