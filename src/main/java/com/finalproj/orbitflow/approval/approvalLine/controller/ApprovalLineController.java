package com.finalproj.orbitflow.approval.approvalLine.controller;

import com.finalproj.orbitflow.approval.approvalLine.dto.ApprovalLineUpdateReqDto;
import com.finalproj.orbitflow.approval.approvalLine.dto.ApprovalRuleResDto;
import com.finalproj.orbitflow.approval.approvalLine.service.ApprovalLineService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : ApprovalLineController
 * @since : 25. 12. 25. 목요일
 **/


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApprovalLineController {

    private final ApprovalLineService approvalLineService;

    @GetMapping("/approval-lines/{documentId}")
    public ResponseEntity<ResponseDto>  getApprovalLinesByDocumentId(
            @PathVariable Long documentId
    ) {
        List<ApprovalRuleResDto> result = approvalLineService.getApprovalLinesByDocumentId(documentId);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "결재선 조회 성공", result));
    }

    @PatchMapping("/approval-lines/{approvalLineId}")
    public ResponseEntity<ResponseDto>  updateApprovalLine(
            @PathVariable Long approvalLineId,
            @RequestBody ApprovalLineUpdateReqDto reqDto
            ) {
        approvalLineService.updateApprovalLine(approvalLineId, reqDto.getApproverId());
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "결재선 지정 성공",  null));
    }
}
