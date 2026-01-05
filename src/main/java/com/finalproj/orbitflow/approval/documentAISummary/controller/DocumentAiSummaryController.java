package com.finalproj.orbitflow.approval.documentAISummary.controller;

import com.finalproj.orbitflow.approval.documentAISummary.service.DocumentAiSummaryService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentAiSummaryController
 * @since : 26. 1. 5. 월요일
 **/

@RestController
@RequestMapping("/api/document-ai-summary")
@RequiredArgsConstructor
public class DocumentAiSummaryController {

    private final DocumentAiSummaryService documentAiSummaryService;


    @PostMapping("/{documentId}")
    public ResponseEntity<ResponseDto> sendReqSummary(
            @PathVariable Long documentId
    ) {
        documentAiSummaryService.sendReqSummary(SecurityUtils.getEmployeeId(), documentId);
        return null;
    }

}
