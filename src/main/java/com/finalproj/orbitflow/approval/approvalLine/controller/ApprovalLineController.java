package com.finalproj.orbitflow.approval.approvalLine.controller;

import com.finalproj.orbitflow.approval.approvalLine.service.ApprovalAutoLineAppService;
import com.finalproj.orbitflow.approval.approvalLine.service.ApprovalLineService;
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
 * @filename : ApprovalLineController
 * @since : 25. 12. 25. 목요일
 **/


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApprovalLineController {

    private final ApprovalLineService approvalLineService;
    private final ApprovalAutoLineAppService approvalAutoLineAppService;

    @PostMapping("/approval-lines/draft/{formTemplateId}")
    public ResponseEntity<ResponseDto>  createApprovalLineByFormTemplate(
            @PathVariable Long formTemplateId,
            @RequestParam Long documentId
    ) {

        approvalAutoLineAppService.generate(SecurityUtils.getEmployeeId(), formTemplateId, documentId);

        return ResponseEntity.ok(new ResponseDto(HttpStatus.CREATED, "임시 결재선 생성 성공", null));
    }

}
