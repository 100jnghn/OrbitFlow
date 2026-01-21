package com.finalproj.orbitflow.approval.form.template.group.controller;

import com.finalproj.orbitflow.approval.form.template.group.dto.FormTemplateGroupCreateReqDto;
import com.finalproj.orbitflow.approval.form.template.group.dto.FormTemplateGroupUpdateReqDto;
import com.finalproj.orbitflow.approval.form.template.group.service.FormTemplateGroupService;
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
 * @filename : FormTemplateGroupController
 * @since : 25. 12. 16. 화요일
 **/


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FormTemplateGroupController {

    private final FormTemplateGroupService formTemplateGroupService;

    @GetMapping("/form-template-groups")
    public ResponseEntity<?> formTemplateGroups(
            @RequestParam(required = false) String keyword
    ) {

        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "양식 그룹 목록 조회",
                        formTemplateGroupService.getFormTemplateGroups(SecurityUtils.getCompanyId(), keyword)));
    }


    @PostMapping("/admin/form-template-groups")
    public ResponseEntity<?> saveFormTemplateGroup(
            @RequestBody FormTemplateGroupCreateReqDto dto
    ) {
        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.CREATED,
                        "양식 그룹 생성 성공",
                        formTemplateGroupService.saveFormTemplateGroup(dto, SecurityUtils.getCompanyId()
                        )
                )
        );
    }

    @GetMapping("/form-template-groups/{id}")
    public ResponseEntity<?> getDetailFormTemplateGroup(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "양식 그룹 상세 조회 성공",
                        formTemplateGroupService.getDetailTemplateGroup(id)));
    }

    @PatchMapping("/admin/form-template-groups/{id}")
    public ResponseEntity<?> updateFormTemplateGroup(
            @PathVariable Long id,
            @RequestBody FormTemplateGroupUpdateReqDto dto
    ) {
        formTemplateGroupService.updateFormTemplateGroup(dto, id);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "양식 그룹 수정 완료", null));
    }


    @GetMapping("/admin/form-template-groups/check-name")
    public ResponseEntity<?> checkFormTemplateGroupName(
            @RequestParam String name
    ) {
        Boolean result = formTemplateGroupService.checkFormTemplateGroupName(SecurityUtils.getCompanyId(), name);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "중복 체크 결과 반환", result));
    }
}
