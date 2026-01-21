package com.finalproj.orbitflow.approval.document.documentContent.controller;

import com.finalproj.orbitflow.approval.document.documentContent.dto.DocumentContentPatchReqDto;
import com.finalproj.orbitflow.approval.document.documentContent.service.DocumentContentService;
import com.finalproj.orbitflow.approval.form.template.schema.FormTemplateSchema;
import com.finalproj.orbitflow.global.common.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentContentController
 * @since : 25. 12. 23. 화요일
 **/

@RestController
@RequestMapping("/api/document-contents")
@RequiredArgsConstructor
public class DocumentContentController {
    private final DocumentContentService documentContentService;

    @GetMapping("/{documentId}")
    public ResponseEntity<ResponseDto> getDocumentContent(
            @PathVariable("documentId") Long documentId
    ) {
        FormTemplateSchema result = documentContentService.getDocumentContentByDocumentId(documentId);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "문서 내용 조회 성공",  result));
    }

    @PatchMapping("/{documentId}")
    public ResponseEntity<ResponseDto> updateDocumentContent(
            @PathVariable Long documentId,
            @RequestBody DocumentContentPatchReqDto reqDto
    ) {
        documentContentService.patchContent(documentId, reqDto);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "문서 내용 수정 성공", null));
    }
}
