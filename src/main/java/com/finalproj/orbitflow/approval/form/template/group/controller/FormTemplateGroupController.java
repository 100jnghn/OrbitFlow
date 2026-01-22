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
 * 결재 양식 그룹(FormTemplateGroup)과 관련된 REST API 요청을 처리하는 컨트롤러이다.
 * <p>
 * 양식 그룹은 여러 개의 결재 양식(FormTemplate)을 묶는 상위 개념으로,
 * 회사 단위로 관리되며 결재 양식의 분류 기준 역할을 한다.
 * <p>
 * 이 컨트롤러에서는
 * - 전체 양식 그룹 목록 조회
 * - 특정 양식 그룹 상세 조회
 * - 양식 그룹 생성 및 수정
 * - 양식 그룹명 중복 여부 확인
 * 과 같은 기능을 제공한다.
 * <p>
 * 조회 API는 일반 사용자도 접근 가능하지만,
 * 생성 및 수정과 같이 관리가 필요한 기능은 관리자 권한을 전제로 한다.
 * <p>
 * 컨트롤러는 요청 파라미터 처리와 응답 반환까지만 담당하며,
 * 실제 비즈니스 로직과 검증은 FormTemplateGroupService에 위임한다.
 * <p>
 * 모든 양식 그룹 데이터는 회사 단위(companyId)로 구분되며,
 * 현재 로그인한 사용자의 회사 정보는 SecurityUtils를 통해 조회한다.
 *
 * @author Choi MinHyeok
 * @filename FormTemplateGroupController
 * @since 2025. 12. 16.
 */


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
