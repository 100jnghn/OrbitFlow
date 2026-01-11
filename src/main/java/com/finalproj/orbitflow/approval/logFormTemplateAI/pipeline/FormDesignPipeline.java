package com.finalproj.orbitflow.approval.logFormTemplateAI.pipeline;

import com.finalproj.orbitflow.approval.logFormTemplateAI.dto.AiFormDesignResult;
import com.finalproj.orbitflow.approval.logFormTemplateAI.dto.FormDesignReqContext;
import com.finalproj.orbitflow.approval.logFormTemplateAI.dto.FormDesignResContext;
import com.finalproj.orbitflow.approval.logFormTemplateAI.dto.FormTemplateJson;
import com.finalproj.orbitflow.approval.logFormTemplateAI.pipeline.assembler.FormTemplateAssembler;
import com.finalproj.orbitflow.approval.logFormTemplateAI.pipeline.processor.AiFormResultProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormDesignPipeline
 * @since : 26. 1. 8. 목요일
 **/


@Component
@RequiredArgsConstructor
public class FormDesignPipeline {

    private final List<AiFormResultProcessor> processors;
    private final FormTemplateAssembler assembler;

    public PipelineResult run(AiFormDesignResult parsed, FormDesignReqContext reqCtx) {
        FormDesignResContext resCtx = new FormDesignResContext();
        AiFormDesignResult cur = parsed;

        for (AiFormResultProcessor p : processors) {
            cur = p.process(cur, reqCtx, resCtx);
        }

        FormTemplateJson templateJson = assembler.assemble(cur, reqCtx.formName());

        return new PipelineResult(templateJson, resCtx);
    }

    public record PipelineResult(
            FormTemplateJson processed,
            FormDesignResContext responseContext
    ) {
    }
}
