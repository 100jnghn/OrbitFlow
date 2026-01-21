package com.finalproj.orbitflow.approval.form.template.ai.pipeline;

import com.finalproj.orbitflow.approval.form.template.ai.dto.AiFormDesignResult;
import com.finalproj.orbitflow.approval.form.template.ai.dto.FormDesignReqContext;
import com.finalproj.orbitflow.approval.form.template.ai.dto.FormDesignResContext;
import com.finalproj.orbitflow.approval.form.template.ai.dto.FormTemplateJson;
import com.finalproj.orbitflow.approval.form.template.ai.pipeline.assembler.FormTemplateAssembler;
import com.finalproj.orbitflow.approval.form.template.ai.pipeline.processor.AiFormResultProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI가 생성한 양식 설계 결과를
 * 최종적으로 사용할 수 있는 결재 양식 구조로 완성하기 위한
 * 전체 처리 흐름을 담당하는 파이프라인 클래스이다.
 * <p>
 * 이 파이프라인은 AI 응답을 그대로 사용하지 않고,
 * 여러 단계의 Processor를 순차적으로 적용하여
 * 정책 보정, 중복 제거, 형태 안정화 등의 후처리를 수행한다.
 * <p>
 * 각 Processor는 하나의 책임만을 가지며,
 * Spring의 Order 설정에 따라 정해진 순서대로 실행된다.
 * 이를 통해 AI 결과를 단계적으로 다듬는 구조를 유지한다.
 * <p>
 * 모든 Processor 처리가 완료되면,
 * 최종 결과를 FormTemplateAssembler에 전달하여
 * 실제 저장 및 사용이 가능한 FormTemplateJson으로 변환한다.
 * <p>
 * 처리 과정에서 적용된 규칙이나 보정 내역은
 * FormDesignResContext에 누적되며,
 * 이는 호출자에게 함께 반환되어
 * AI 판단 결과를 추적하거나 디버깅하는 데 사용된다.
 * <p>
 * 즉, 이 클래스는
 * "AI 설계 결과 → 정책 적용 → 최종 양식 생성"
 * 이라는 전체 흐름을 묶어주는 조정자 역할을 수행한다.
 *
 * @author Choi MinHyeok
 * @filename FormDesignPipeline
 * @since 2026. 1. 8.
 */


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
