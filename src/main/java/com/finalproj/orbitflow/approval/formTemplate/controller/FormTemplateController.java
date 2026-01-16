package com.finalproj.orbitflow.approval.formTemplate.controller;

import com.finalproj.orbitflow.approval.formTemplate.dto.*;
import com.finalproj.orbitflow.approval.formTemplate.service.FormTemplateService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 결재 양식(FormTemplate) 관련 REST API 컨트롤러
 * <p>
 * - 관리자: 생성 / 수정 / 복제 / 활성화 / 미리보기 / 전체 조회
 * - 일반 사용자: 활성 양식 조회 / 상세 조회
 * <p>
 *
 * @author Choi MinHyeok
 * @since 25.12.17
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FormTemplateController {
    private final FormTemplateService formTemplateService;

    @PostMapping("/admin/form-templates")
    public ResponseEntity<?> saveFormTemplate(
            @RequestParam Long templateGroupId
    ) {
        Long formTemplateId = formTemplateService.saveFormTemplate(
                templateGroupId,
                SecurityUtils.getCompanyId()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDto<>(
                        HttpStatus.CREATED,
                        "결재 양식 초안이 생성되었습니다.",
                        Map.of("formTemplateId", formTemplateId)
                ));
    }

    @DeleteMapping("/admin/form-templates/draft/{formTemplateId}")
    public ResponseEntity<?> deleteDraftFormTemplate(
            @PathVariable Long formTemplateId
    ) {
        formTemplateService.deleteDraftFormTemplate(formTemplateId);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.NO_CONTENT, "임시 양식 삭제 성공", null));
    }


    @PostMapping("/admin/form-templates/{templateGroupId}/revise")
    public ResponseEntity<?> reviseFormTemplate(
            @PathVariable Long templateGroupId
    ) {
        Long createdTemplateId =
                formTemplateService.reviseFormTemplateByTemplateGroup(
                        templateGroupId,
                        SecurityUtils.getCompanyId()
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDto<>(
                        HttpStatus.CREATED,
                        "결재 양식이 복제되었습니다.",
                        Map.of("createdTemplateId", createdTemplateId)
                ));
    }

    @PatchMapping("/admin/form-templates/{formTemplateId}/structure")
    public ResponseEntity<?> updateFormTemplateStructure(
            @PathVariable Long formTemplateId,
            @RequestBody FormTemplateUpdateReqDto reqDto
    ) {
        formTemplateService.updateStructure(
                formTemplateId,
                SecurityUtils.getCompanyId(),
                reqDto
        );

        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "결재 양식 구조가 수정되었습니다.",
                        null
                )
        );
    }

    @PatchMapping("/admin/form-templates/{formTemplateId}/approval-rule")
    public ResponseEntity<?> updateFormTemplateApprovalRule(
            @PathVariable Long formTemplateId,
            @RequestBody FormTemplateUpdateReqDto reqDto
    ) {
        formTemplateService.updateApprovalRule(
                formTemplateId,
                SecurityUtils.getCompanyId(),
                reqDto
        );

        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "결재선 규칙이 수정되었습니다.",
                        null
                )
        );
    }

    @PostMapping("/admin/form-templates/{formTemplateId}/publish")
    public ResponseEntity<?> publishFormTemplate(
            @PathVariable Long formTemplateId
    ) {
        formTemplateService.publishFormTemplate(
                formTemplateId,
                SecurityUtils.getCompanyId()
        );

        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "결재 양식이 활성화되었습니다.",
                        null
                )
        );
    }

    @GetMapping("/admin/form-templates/{formTemplateId}/preview")
    public ResponseEntity<?> previewFormTemplate(
            @PathVariable Long formTemplateId
    ) {
        FormTemplatePreviewResDto result =
                formTemplateService.getPreviewFormTemplate(
                        SecurityUtils.getCompanyId(),
                        formTemplateId
                );

        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "양식 구조 메타 데이터 반환 성공",
                        result
                )
        );
    }

    @GetMapping("/admin/form-templates/all")
    public ResponseEntity<?> getAllFormTemplates(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int size,
            @ModelAttribute FormTemplateAllListReqDto reqDto
    ) {
        Page<FormTemplateAllListResDto> result =
                formTemplateService.allFormTemplate(
                        SecurityUtils.getCompanyId(),
                        size,
                        offset,
                        reqDto
                );

        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "문서 양식 전체 조회",
                        result
                )
        );
    }

    @GetMapping("/form-templates/active")
    public ResponseEntity<?> getActiveFormTemplates(
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "활성 상태 양식 목록 조회",
                        formTemplateService.getActiveFormTemplates(
                                SecurityUtils.getCompanyId(),
                                keyword
                        )
                )
        );
    }

    @GetMapping("/form-templates/{formTemplateId}")
    public ResponseEntity<?> getDetailFormTemplate(
            @PathVariable Long formTemplateId
    ) {
        FormTemplateDetailResDto result =
                formTemplateService.getDetailFormTemplate(
                        formTemplateId,
                        SecurityUtils.getCompanyId()
                );

        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "문서 양식 상세 조회 성공",
                        result
                )
        );
    }
}
