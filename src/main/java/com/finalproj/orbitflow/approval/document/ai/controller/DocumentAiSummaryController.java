package com.finalproj.orbitflow.approval.document.ai.controller;

import com.finalproj.orbitflow.approval.document.ai.dto.AiSummaryResDto;
import com.finalproj.orbitflow.approval.document.ai.enums.SummaryType;
import com.finalproj.orbitflow.approval.document.ai.service.DocumentAiSummaryService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 결재 문서에 대한 AI 요약 및 변경 비교(diff) 기능을 제공하는 REST 컨트롤러.
 * <p>
 * AI 요약/비교 요청은 비동기 방식으로 처리되며,
 * 본 컨트롤러는 생성 요청과 결과 조회 API를 분리하여 제공한다.
 * <p>
 * 요약 및 비교 요청 시에는 즉시 결과를 반환하지 않고,
 * AI 처리 작업을 시작하도록 요청만 전달한다.
 * 이후 클라이언트는 별도의 조회 API를 통해
 * 처리 상태(PROCESSING / COMPLETED / FAILED)와 결과를 확인한다.
 * <p>
 * 실제 AI 처리 로직은 DocumentAiSummaryService에 위임하며,
 * 본 컨트롤러는 요청 수신, 인증 주체 식별,
 * 그리고 공통 응답 포맷 반환만을 담당한다.
 *
 * @author : Choi MinHyeok
 * @filename : DocumentAiSummaryController
 * @since : 26. 1. 5. 월요일
 */


@RestController
@RequestMapping("/api/document-ai")
@RequiredArgsConstructor
public class DocumentAiSummaryController {

    private final DocumentAiSummaryService documentAiSummaryService;


    @PostMapping("/{documentId}/summary")
    public ResponseEntity<?> sendReqSummary(
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
    public ResponseEntity<?> sendReqDiff(
            @PathVariable Long documentId
    ) {
        documentAiSummaryService.sendReqDiff(SecurityUtils.getEmployeeId(), documentId);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.ACCEPTED, "ai 문서 비교 생성 시작", null));
    }


}
