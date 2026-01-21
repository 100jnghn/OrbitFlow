package com.finalproj.orbitflow.approval.form.template.ai.pipeline.assembler;

import com.finalproj.orbitflow.approval.form.template.ai.dto.AiFormDesignResult;
import com.finalproj.orbitflow.approval.form.template.ai.dto.FormTemplateJson;
import com.finalproj.orbitflow.approval.form.template.ai.service.FormTemplateBuildService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateAssembler
 * @since : 26. 1. 8. 목요일
 **/


@Component
@RequiredArgsConstructor
public class FormTemplateAssembler {

    private final FormTemplateBuildService buildService;

    public FormTemplateJson assemble(AiFormDesignResult result, String forName) {
        return buildService.build(result, forName);
    }
}