package com.finalproj.orbitflow.approval.documentAISummary.aiBuilder;

import com.finalproj.orbitflow.approval.documentAISummary.dto.AiSummaryReqDto;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AiSummaryPromptBuilder
 * @since : 26. 1. 5. 월요일
 **/


@Component
public class AiSummaryPromptBuilder {

    public String build(AiSummaryReqDto request) {

        StringBuilder sb = new StringBuilder();

        sb.append("""
                너는 사내 결재 문서를 요약하는 AI다.
                아래 정보를 바탕으로 간결한 요약문을 작성하라.
                
                조건:
                - 불필요한 수식어 제거
                - 핵심 정보 중심
                - 한글로 작성
                - 3~5문장 이내
                
                """);

        sb.append("문서 제목: ")
                .append(request.getDocumentTitle())
                .append("\n\n");

        sb.append("[핵심 정보]\n");
        request.getCoreFields().forEach(f ->
                sb.append("- ")
                        .append(f.getLabel())
                        .append(": ")
                        .append(f.getValue())
                        .append("\n")
        );

        if (!request.getOptionalFields().isEmpty()) {
            sb.append("\n[추가 정보]\n");
            request.getOptionalFields().forEach(f ->
                    sb.append("- ")
                            .append(f.getLabel())
                            .append(": ")
                            .append(f.getValue())
                            .append("\n")
            );
        }

        return sb.toString();
    }
}
