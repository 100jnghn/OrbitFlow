package com.finalproj.orbitflow.approval.documentAISummary.controller;

import com.finalproj.orbitflow.approval.documentAISummary.dto.AiSummaryResDto;
import com.finalproj.orbitflow.approval.documentAISummary.enums.SummaryType;
import com.finalproj.orbitflow.approval.documentAISummary.service.DocumentAiSummaryService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentAiSummaryController
 * @since : 26. 1. 5. 월요일
 **/

@RestController
@RequestMapping("/api/document-ai")
@RequiredArgsConstructor
public class DocumentAiSummaryController {

    private final DocumentAiSummaryService documentAiSummaryService;


    @PostMapping("/{documentId}/summary")
    public ResponseEntity<ResponseDto> sendReqSummary(
            @PathVariable Long documentId
    ) {
        documentAiSummaryService.sendReqSummary(SecurityUtils.getEmployeeId(), documentId);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.ACCEPTED, "ai 요약 생성 시작", null));
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<?> readSummary(
            @PathVariable Long documentId,
            @RequestParam SummaryType summaryType
    ) {
        AiSummaryResDto result =
                documentAiSummaryService.readSummary(
                        SecurityUtils.getEmployeeId(),
                        documentId,
                        summaryType
                );

        if (result == null) {
            // 요약 row 자체가 아직 없는 경우
            return ResponseEntity.ok(
                    new ResponseDto<>(
                            HttpStatus.OK,
                            "ai 요약 생성 전",
                            null
                    )
            );
        }
        return switch (result.getAiStatus()) {
            case PROCESSING -> ResponseEntity.ok(
                    new ResponseDto<>(
                            HttpStatus.OK,
                            "ai 요약 생성 중",
                            result
                    )
            );
            case COMPLETED -> ResponseEntity.ok(
                    new ResponseDto<>(
                            HttpStatus.OK,
                            "ai 요약 조회 성공",
                            result
                    )
            );
            case FAILED -> ResponseEntity.ok(
                    new ResponseDto<>(
                            HttpStatus.OK,
                            "ai 요약 생성 실패",
                            result
                    )
            );
        };
    }

    @PostMapping("/{documentId}/diff")
    public ResponseEntity<ResponseDto> sendReqDiff(
            @PathVariable Long documentId
    ) {
        documentAiSummaryService.sendReqDiff(SecurityUtils.getEmployeeId(), documentId);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.ACCEPTED, "ai 문서 비교 생성 시작", null));
    }


}
