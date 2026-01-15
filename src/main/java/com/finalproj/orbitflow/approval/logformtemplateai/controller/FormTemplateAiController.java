package com.finalproj.orbitflow.approval.logformtemplateai.controller;

import com.finalproj.orbitflow.approval.logformtemplateai.dto.FormTemplateAiReqDto;
import com.finalproj.orbitflow.approval.logformtemplateai.dto.FormTemplateAiResDto;
import com.finalproj.orbitflow.approval.logformtemplateai.service.FormTemplateAiService;
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
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateAiController
 * @since : 26. 1. 7. 수요일
 **/


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
        log.info("[START] generateFormTemplate]");
        FormTemplateAiResDto result = formTemplateAiService.requestFormTemplate(
                SecurityUtils.getEmployeeId(),
                reqDto
        );

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.CREATED, "Ai 양식 생성 성공", result));
    }
}