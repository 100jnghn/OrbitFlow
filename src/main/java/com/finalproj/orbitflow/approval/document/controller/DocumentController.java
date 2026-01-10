package com.finalproj.orbitflow.approval.document.controller;

import com.finalproj.orbitflow.approval.approvalLine.dto.ReferenceCreateReqDto;
import com.finalproj.orbitflow.approval.document.dto.*;
import com.finalproj.orbitflow.approval.document.service.DocumentApplicationService;
import com.finalproj.orbitflow.approval.document.service.DocumentService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private final DocumentApplicationService documentApplicationService;


    @GetMapping("/my-written")
    public ResponseEntity<?> getMyWrittenDocuments(
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "10") int size,
            DocumentListReqDto reqDto
    ) {

        Page<DocumentListResDto> result = documentService.getMyWrittenDocuments(SecurityUtils.getCompanyId(), SecurityUtils.getEmployeeId(), offset, size, reqDto);

        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "나의 기안 목록 조회 성공", result));
    }


    @GetMapping("/approvals")
    public ResponseEntity<?> getDocumentsToApprove(
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "10") int size,
            DocumentListReqDto reqDto
    ) {
        Page<DocumentMyApprovalListResDto> documentsToApprove = documentService.getDocumentsToApprove(SecurityUtils.getCompanyId(), SecurityUtils.getEmployeeId(), offset, size, reqDto);
        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "내 결재 목록 조회 성공", documentsToApprove));
    }

    @PostMapping("/draft/{formTemplateId}")
    public ResponseEntity<?> createDocument(
            @PathVariable Long formTemplateId,
            @RequestParam(required = false) Long beforeDocumentId
    ) {
        DocumentCreateResDto result = documentApplicationService.createDraft(SecurityUtils.getCompanyId(), SecurityUtils.getEmployeeId(), formTemplateId, beforeDocumentId);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.CREATED, "결재 문서 초안 생성 성공", result));
    }

    @PostMapping("/{documentId}/revise")
    public ResponseEntity<?> reviseDocument(
            @PathVariable Long documentId
    ) {
        DocumentCreateResDto result = documentApplicationService.reviseDocument(SecurityUtils.getEmployeeId(), documentId);

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.CREATED, "반려 결재 문서 복제 성공", result));
    }

    @GetMapping("/{documentId}/revision")
    public ResponseEntity<?> getDocumentRevision(
            @PathVariable Long documentId
    ) {
        DocumentRevisionInfoResDto result = documentService.getDocumentRevision(SecurityUtils.getEmployeeId(), documentId);

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "재기안 문서 조회 성공", result));
    }

    @PatchMapping("/update/{DocumentId}")
    public ResponseEntity<?> updateDocument(
            @PathVariable Long DocumentId,
            @RequestBody DocumentUpdateReqDto reqDto
    ) {
        documentService.updateDocument(SecurityUtils.getEmployeeId(), DocumentId, reqDto);

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "문서 수정 성공", null));
    }


    @PostMapping("/{documentId}/submit")
    public ResponseEntity<?> submitDocument(
            @PathVariable Long documentId
    ) {
        documentApplicationService.submitDocument(SecurityUtils.getEmployeeId(), documentId);

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "문서 상신 성공", null));
    }

    @GetMapping("/{documentId}/detail")
    public ResponseEntity<?> getDocumentDetail(
            @PathVariable Long documentId
    ) {
        DocumentDetailResDto result = documentService.getDocumentDetail(SecurityUtils.getEmployeeId(), documentId);

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "문서 상세 조회 성공", result));
    }

    @PostMapping("/{documentId}/approve")
    public ResponseEntity<?> approveDocument(
            @PathVariable Long documentId,
            @RequestBody(required = false) DocumentCommentReqDto reqDto
    ) {
        String comment = reqDto != null ? reqDto.getComment() : null;

        documentApplicationService.approve(
                SecurityUtils.getEmployeeId(),
                documentId,
                comment
        );

        return ResponseEntity.ok(
                new ResponseDto<>(HttpStatus.OK, "승인 처리 성공", null)
        );
    }


    @PostMapping("/{documentId}/reject")
    public ResponseEntity<?> rejectDocument(
            @PathVariable Long documentId,
            @RequestBody(required = false) DocumentCommentReqDto reqDto
    ) {
        String comment = reqDto != null ? reqDto.getComment() : null;

        documentApplicationService.reject(
                SecurityUtils.getEmployeeId(),
                documentId,
                comment
        );

        return ResponseEntity.ok(
                new ResponseDto<>(HttpStatus.OK, "반려 처리 성공", null)
        );
    }


    @GetMapping("/reference/search")
    public ResponseEntity<?> searchReference(
            @RequestParam(defaultValue = "") String keyword
    ) {

        List<ReferenceSearchResDto> result = documentService.searchReference(
                SecurityUtils.getEmployeeId(),
                SecurityUtils.getCompanyId(),
                keyword
        );

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "참조 문서 검색 성공", result));
    }

    @PostMapping("/{documentId}/reference")
    public ResponseEntity<?> addReferenceDocument(
            @PathVariable Long documentId,
            @RequestBody ReferenceCreateReqDto request
    ) {
        documentService.addReferenceDocument(
                SecurityUtils.getEmployeeId(),
                documentId,
                request.getTargetDocumentFileId()
        );
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{documentId}/reference")
    public ResponseEntity<?> updateReferenceDocument(
            @PathVariable Long documentId,
            @RequestParam Long documentFileId
    ) {
        documentService.removeReferenceDocument(
                SecurityUtils.getEmployeeId(),
                documentId,
                documentFileId
        );
        return ResponseEntity.ok().build();
    }

}
