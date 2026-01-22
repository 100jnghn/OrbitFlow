package com.finalproj.orbitflow.approval.document.content.controller;

import com.finalproj.orbitflow.approval.document.content.dto.DocumentContentPatchReqDto;
import com.finalproj.orbitflow.approval.document.content.service.DocumentContentService;
import com.finalproj.orbitflow.approval.form.template.schema.FormTemplateSchema;
import com.finalproj.orbitflow.global.common.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 결재 문서 작성 화면에서 사용하는 문서 내용 조회 및 수정 컨트롤러.
 * <p>
 * 문서에 연결된 FormTemplateSchema 기반의 작성 데이터를 조회하거나,
 * 사용자가 입력한 내용에 대해 부분 수정(PATCH)을 처리한다.
 * <p>
 * 컨트롤러에서는 요청 수신과 응답만 담당하며,
 * 실제 문서 내용 조회 및 수정 로직은 DocumentContentService에 위임한다.
 *
 * @author : Choi MinHyeok
 * @filename : DocumentContentController
 * @since : 25. 12. 23. 화요일
 */


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
