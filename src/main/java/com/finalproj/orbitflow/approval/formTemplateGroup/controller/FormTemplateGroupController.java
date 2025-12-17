package com.finalproj.orbitflow.approval.formTemplateGroup.controller;

import com.finalproj.orbitflow.approval.formTemplateGroup.dto.FormTemplateGroupCreateReqDto;
import com.finalproj.orbitflow.approval.formTemplateGroup.dto.FormTemplateGroupCreateResDto;
import com.finalproj.orbitflow.approval.formTemplateGroup.dto.FormTemplateGroupUpdateReqDto;
import com.finalproj.orbitflow.approval.formTemplateGroup.service.FormTemplateGroupService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping("form-template-groups")
    public ResponseEntity<ResponseDto> formTemplateGroups(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) String keyword
    ) {

        return ResponseEntity.ok(
                new ResponseDto(
                        HttpStatus.OK,
                        "양식 그룹 목록 조회",
                        formTemplateGroupService.getFormTemplateGroups(user.getCompanyId(), keyword)));
    }


    @PostMapping("admin/form-template-groups")
    public ResponseEntity<ResponseDto> saveFormTemplateGroup(
            @RequestBody FormTemplateGroupCreateReqDto dto,
            @AuthenticationPrincipal SecurityUser user
    ) {

        FormTemplateGroupCreateResDto result = new FormTemplateGroupCreateResDto(formTemplateGroupService.saveFormTemplateGroup(dto, user.getCompanyId()));

        return ResponseEntity.ok(new ResponseDto(HttpStatus.CREATED, "양식 그룹 생성 성공", result));
    }

    @GetMapping("form-template-groups/{id}")
    public ResponseEntity<ResponseDto> getDetailFormTemplateGroup(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                new ResponseDto(
                        HttpStatus.OK,
                        "양식 그룹 상세 조회 성공",
                        formTemplateGroupService.getDetailTemplateGroup(id)));
    }

    @PatchMapping("admin/form-template-groups/{id}")
    public ResponseEntity<ResponseDto> updateFormTemplateGroup(
            @PathVariable Long id,
            @RequestBody FormTemplateGroupUpdateReqDto dto
    ) {
        formTemplateGroupService.updateFormTemplateGroup(dto, id);
        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "양식 그룹 수정 완료", null));
    }
    
}
