package com.finalproj.orbitflow.approval.form.template.controller;

import com.finalproj.orbitflow.approval.form.template.dto.*;
import com.finalproj.orbitflow.approval.form.template.service.FormTemplateService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 결재 양식(FormTemplate)과 관련된 모든 REST API 요청을 처리하는 컨트롤러이다.
 * <p>
 * 이 컨트롤러는 관리자와 일반 사용자의 접근 범위를 구분하여,
 * 결재 양식의 생성부터 수정, 개정, 활성화, 조회까지의 전체 흐름을 담당한다.
 * <p>
 * 관리자 권한으로는
 * - 결재 양식 초안 생성
 * - 기존 양식 복제(개정)
 * - 양식 구조 및 결재선 규칙 수정
 * - 양식 활성화(배포)
 * - 양식 미리보기 및 전체 목록 조회
 * 기능을 제공한다.
 * <p>
 * 일반 사용자에게는
 * - 활성화된 결재 양식 목록 조회
 * - 특정 결재 양식의 상세 정보 조회
 * 기능만을 허용한다.
 * <p>
 * 이 컨트롤러는 HTTP 요청과 응답 처리, 그리고
 * 현재 로그인한 사용자 및 회사 정보 식별까지만 담당하며,
 * 실제 비즈니스 로직은 FormTemplateService에 위임한다.
 * <p>
 * 모든 API는 회사 단위(companyId)로 동작하며,
 * 회사 정보는 SecurityUtils를 통해 조회한다.
 *
 * @author Choi MinHyeok
 * @filename FormTemplateController
 * @since 2025. 12. 17.
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
