package com.finalproj.orbitflow.approval.form.template.ai.controller;

import com.finalproj.orbitflow.approval.form.template.ai.dto.FormTemplateAiReqDto;
import com.finalproj.orbitflow.approval.form.template.ai.dto.FormTemplateAiResDto;
import com.finalproj.orbitflow.approval.form.template.ai.service.FormTemplateAiService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI를 이용해 결재 양식을 생성하기 위한 API 요청을 처리하는 컨트롤러.
 * <p>
 * 관리자가 AI 양식 생성을 요청하면,
 * 입력된 요청 정보와 현재 로그인한 사용자 정보를 기반으로
 * AI 양식 생성 서비스를 호출하고 그 결과를 반환한다.
 * <p>
 * 이 컨트롤러는 요청 검증과 사용자 식별까지만 담당하며,
 * 실제 AI 호출, 프롬프트 구성, 생성 결과 처리 로직은
 * 서비스 계층(FormTemplateAiService)에서 처리한다.
 * <p>
 * AI 생성 결과는 즉시 클라이언트에 반환되며,
 * 이후 양식 저장 또는 추가 편집 여부는 별도의 흐름에서 결정된다.
 *
 * @author Choi MinHyeok
 * @filename FormTemplateAiController
 * @since 2026. 1. 7.
 */


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/form-template/ai")
public class FormTemplateAiController {

    private final FormTemplateAiService formTemplateAiService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateFormTemplate(
            @Valid @RequestBody FormTemplateAiReqDto reqDto
    ) {
        FormTemplateAiResDto result = formTemplateAiService.requestFormTemplate(
                SecurityUtils.getEmployeeId(),
                reqDto
        );
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.CREATED, "Ai 양식 생성 성공", result));
    }
}