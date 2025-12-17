package com.finalproj.orbitflow.approval.formTemplate.controller;

import com.finalproj.orbitflow.approval.formTemplate.dto.FormTemplateUpdateReqDto;
import com.finalproj.orbitflow.approval.formTemplate.service.FormTemplateService;
import com.finalproj.orbitflow.approval.templateCategory.enums.TemplateCategoryCode;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam Long templateGroupId,
            @RequestParam TemplateCategoryCode categoryCode
    ) {
        Long formTemplateId = formTemplateService.saveFormTemplate(
                templateGroupId,
                user.getCompanyId(),
                categoryCode
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(
                        HttpStatus.CREATED,
                        "결재 양식 초안이 생성되었습니다.",
                        Map.of("formTemplateId", formTemplateId)));
    }

    @PostMapping("/admin/form-templates/{id}/revise")
    public ResponseEntity<ResponseDto> reviseFormTemplate(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam Long templateGroupId
    ) {
        Long formTemplateId = formTemplateService.reviseFormTemplateByTemplateGroup(templateGroupId, user.getCompanyId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(
                        HttpStatus.CREATED,
                        "결재 양식이 복제되었습니다.",
                        Map.of("formTemplateId", formTemplateId)));
    }


    @PatchMapping("/admin/form-templates/{id}/structure")
    public ResponseEntity<ResponseDto> updateFormTemplateStructure(
            @AuthenticationPrincipal SecurityUser user,
            @RequestBody FormTemplateUpdateReqDto reqDto,
            @PathVariable(name = "id") Long formTemplateId
    ) {
        formTemplateService.updateStructure(formTemplateId, user.getCompanyId(), reqDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto(HttpStatus.OK, "결재 양식 구조가 수정되었습니다.", null));
    }

    @PatchMapping("/admin/form-templates/{id}/approval-rule")
    public ResponseEntity<ResponseDto> updateFormTemplateApprovalRule(
            @AuthenticationPrincipal SecurityUser user,
            @RequestBody FormTemplateUpdateReqDto reqDto,
            @PathVariable(name = "id") Long formTemplateId
    ) {
        formTemplateService.updateApprovalRule(formTemplateId, user.getCompanyId(), reqDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto(HttpStatus.OK, "결재선 규칙이 수정되었습니다.", null));
    }

    @PostMapping("/admin/form-templates/{id}/publish")
    public ResponseEntity<ResponseDto> publishFormTemplate(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable(name = "id") Long formTemplateId
    ) {
        formTemplateService.publishFormTemplate(formTemplateId, user.getCompanyId());

        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "결재 양식이 활성화되었습니다.", null)
        );
    }

}
