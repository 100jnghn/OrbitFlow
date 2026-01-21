package com.finalproj.orbitflow.approval.document.ai.aiBuilder.summary;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.dto.SummaryPromptContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : SummaryBasePromptComponent
 * @since : 26. 1. 6. 화요일
 **/


@Component
@Order(10)
public class SummaryBasePromptComponent
        implements PromptComponent<SummaryPromptContext> {

    @Override
    public void append(StringBuilder sb, SummaryPromptContext ctx) {
        sb.append("""
                너는 사내 결재 문서를 임원 보고용으로 정리하는 AI다.
                아래에 제공되는 정보들을 종합하여,
                반드시 개괄식(항목형) 요약으로 정리하라.
                
                [출력 규칙]
                - 문단형 서술 금지
                - 각 항목은 하이픈(-)으로 시작
                - 간결한 명사형 또는 짧은 서술형 사용
                - 불필요한 수식어 제거
                - 한글로 작성
                - 추측 금지 (제공된 정보만 사용)
                - 입력 내용을 그대로 복사하지 말고 재구성할 것
                - 동일한 정보(특히 일정, 기간, 대상, 범위 등)는 "주요 내용"과 "참고 정보" 중 한 곳에만 포함할 것
                - 주요 내용에 포함된 정보는 참고 정보에서 반복하지 말 것
                - 참고 정보는 주요 내용에 포함되지 않은 보조 정보만 작성할 것
                
                [출력 형식]
                ■ 문서 개요
                - 문서 제목:
                - 주요 목적:
                
                ■ 주요 내용
                - (핵심 내용 2~4개)
                
                ■ 참고 정보
                - (일정, 대상, 범위 등 문서에 명시된 경우에만)
                
                ■ 첨부 자료
                - (파일명 또는 첨부 존재 여부)
                
                위 형식을 유지하되,
                제공되지 않은 항목은 생략해도 된다.
                
                아래에 이어지는 내용은
                요약 생성을 위한 입력 정보이다.
                입력 정보 자체를 출력하지 말 것.
                
                """);
    }
}

