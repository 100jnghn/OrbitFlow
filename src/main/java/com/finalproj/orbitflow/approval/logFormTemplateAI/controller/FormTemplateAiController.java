package com.finalproj.orbitflow.approval.logFormTemplateAI.controller;

import com.finalproj.orbitflow.approval.logFormTemplateAI.dto.FormTemplateRequestDto;
import com.finalproj.orbitflow.approval.logFormTemplateAI.service.FormTemplateAiService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
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


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/form-template/ai")
public class FormTemplateAiController {

    private final FormTemplateAiService aiService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateFormTemplate(
            @RequestBody FormTemplateRequestDto request
    ) {
        String result = aiService.requestFormTemplate(
                SecurityUtils.getEmployeeId(),
                request.formName(),
                request.purpose()
        );

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.CREATED, "양식 폼 생성 성공", result));
    }
}