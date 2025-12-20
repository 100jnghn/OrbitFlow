package com.finalproj.orbitflow.approval.formTemplate.controller;

import com.finalproj.orbitflow.approval.formTemplate.dto.FormTemplateAllListResDto;
import com.finalproj.orbitflow.approval.formTemplate.dto.FormTemplateDetailResDto;
import com.finalproj.orbitflow.approval.formTemplate.dto.FormTemplateUpdateReqDto;
import com.finalproj.orbitflow.approval.formTemplate.enums.FormTemplateStatus;
import com.finalproj.orbitflow.approval.formTemplate.service.FormTemplateService;
import com.finalproj.orbitflow.approval.templateCategory.enums.TemplateCategoryCode;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateController
 * @since : 25. 12. 17. 수요일
 **/

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FormTemplateController {

    private final FormTemplateService formTemplateService;

    @PostMapping("/admin/form-templates")
    public ResponseEntity<ResponseDto> saveFormTemplate(
            @RequestParam Long templateGroupId,
            @RequestParam TemplateCategoryCode categoryCode
    ) {
        Long formTemplateId = formTemplateService.saveFormTemplate(
                templateGroupId,
                SecurityUtils.getCompanyId(),
                categoryCode
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(
                        HttpStatus.CREATED,
                        "결재 양식 초안이 생성되었습니다.",
                        Map.of("formTemplateId", formTemplateId)));
    }

    @PostMapping("/admin/form-templates/{templateId}/revise")
    public ResponseEntity<ResponseDto> reviseFormTemplate(
            @PathVariable(name = "templateId") Long templateGroupId
    ) {
        Long createdTemplateId = formTemplateService.reviseFormTemplateByTemplateGroup(templateGroupId, SecurityUtils.getCompanyId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(
                        HttpStatus.CREATED,
                        "결재 양식이 복제되었습니다.",
                        Map.of("createdTemplateId", createdTemplateId)));
    }


    @PatchMapping("/admin/form-templates/{templateId}/structure")
    public ResponseEntity<ResponseDto> updateFormTemplateStructure(
            @RequestBody FormTemplateUpdateReqDto reqDto,
            @PathVariable(name = "templateId") Long formTemplateId
    ) {
        formTemplateService.updateStructure(formTemplateId, SecurityUtils.getCompanyId(), reqDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto(HttpStatus.OK, "결재 양식 구조가 수정되었습니다.", null));
    }

    @PatchMapping("/admin/form-templates/{templateId}/approval-rule")
    public ResponseEntity<ResponseDto> updateFormTemplateApprovalRule(
            @RequestBody FormTemplateUpdateReqDto reqDto,
            @PathVariable(name = "templateId") Long formTemplateId
    ) {

        formTemplateService.updateApprovalRule(formTemplateId, SecurityUtils.getCompanyId(), reqDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto(HttpStatus.OK, "결재선 규칙이 수정되었습니다.", null));
    }

    @PostMapping("/admin/form-templates/{templateId}/publish")
    public ResponseEntity<ResponseDto> publishFormTemplate(
            @PathVariable(name = "templateId") Long formTemplateId
    ) {
        formTemplateService.publishFormTemplate(formTemplateId, SecurityUtils.getCompanyId());

        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "결재 양식이 활성화되었습니다.", null)
        );
    }


    @GetMapping("/form-templates/active")
    public ResponseEntity<ResponseDto> activeFormTemplate(
            @RequestParam(required = false) String keyword
    ) {

        return ResponseEntity.ok(
                new ResponseDto(
                        HttpStatus.OK,
                        "활성 상태 양식 목록 조회",
                        formTemplateService.getActiveFormTemplates(SecurityUtils.getCompanyId(), keyword)));
    }


    @GetMapping("/form-templates/{templateId}")
    public ResponseEntity<ResponseDto> getDetailFormTemplate(
            @PathVariable(name = "templateId") Long formTemplateId
    ) {

        FormTemplateDetailResDto result = formTemplateService.getDetailFormTemplate(formTemplateId, SecurityUtils.getCompanyId());

        return ResponseEntity.ok(
                new ResponseDto(
                        HttpStatus.OK,
                        "문서 양식 상세 조회 성공",
                        result
                )
        );
    }

    @GetMapping("/admin/form-templates/all")//status 변수로 추가?
    public ResponseEntity<ResponseDto> getAllFormTemplates(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) FormTemplateStatus status
    ) {
        Page<FormTemplateAllListResDto> result =
                formTemplateService.allFormTemplate(SecurityUtils.getCompanyId(), size, offset, keyword, status);

        return ResponseEntity.ok(
                new ResponseDto(
                        HttpStatus.OK,
                        "문서 양식 전체 조회",
                        result
                )
        );
    }

}
