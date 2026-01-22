package com.finalproj.orbitflow.approval.form.template.ai.pipeline.processor;

import com.finalproj.orbitflow.approval.form.template.ai.dto.AiFormDesignResult;
import com.finalproj.orbitflow.approval.form.template.ai.dto.FormDesignReqContext;
import com.finalproj.orbitflow.approval.form.template.ai.dto.FormDesignResContext;

/**
 * AI가 생성한 양식 설계 결과를 후처리하기 위한 공통 인터페이스이다.
 * <p>
 * 각 구현체는
 * - 입력으로 들어온 AI 설계 결과를 기준으로
 * - 요청 컨텍스트(reqCtx)와 응답 컨텍스트(resCtx)를 참고하여
 * - 필요한 수정, 보완, 검증 작업을 수행한 뒤
 * - 다음 단계로 전달할 결과를 반환한다.
 * <p>
 * 하나의 Processor는 한 가지 책임만 가지도록 설계되며,
 * 여러 Processor가 순차적으로 실행되어
 * 최종적으로 신뢰 가능한 양식 설계 결과를 만들어낸다.
 *
 * @author Choi MinHyeok
 * @filename AiFormResultProcessor
 * @since 2026. 1. 8.
 */


public interface AiFormResultProcessor {
    AiFormDesignResult process(
            AiFormDesignResult input,
            FormDesignReqContext reqCtx,
            FormDesignResContext resCtx
    );
}