package com.finalproj.orbitflow.approval.documentAISummary.aiBuilder;

import com.finalproj.orbitflow.approval.documentAISummary.dto.AiSummaryField;
import com.finalproj.orbitflow.approval.documentAISummary.dto.AiSummaryReqDto;
import org.springframework.stereotype.Component;

import java.util.List;

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
                - 첨부파일이 있는 경우, 문서 맥락 수준에서만 언급
                - 첨부파일의 구체적인 내용은 추측하지 말 것
                
                """);

        // 문서 제목
        sb.append("문서 제목: ")
                .append(request.getDocumentTitle())
                .append("\n\n");

        // 핵심 정보
        appendFields(sb, "[핵심 정보]", request.getCoreFields());

        // 추가 정보
        if (request.getOptionalFields() != null && !request.getOptionalFields().isEmpty()) {
            sb.append("\n");
            appendFields(sb, "[추가 정보]", request.getOptionalFields());
        }

        // 첨부파일 정보 (파일명만)
        List<String> attachmentNames = request.getAttachmentNames();
        if (attachmentNames != null && !attachmentNames.isEmpty()) {
            sb.append("""
                    
                    [첨부 정보]
                    이 문서에는 다음 첨부파일이 포함되어 있다.
                    """);

            attachmentNames.forEach(name ->
                    sb.append("- ").append(name).append("\n")
            );

            sb.append("""
                    
                    ※ 첨부파일의 실제 내용은 제공되지 않았다.
                    파일명 기준으로만 문서의 맥락을 참고하라.
                    """);
        }

        return sb.toString();
    }

    private void appendFields(
            StringBuilder sb,
            String title,
            List<AiSummaryField> fields
    ) {
        if (fields == null || fields.isEmpty()) {
            return;
        }

        sb.append(title).append("\n");
        fields.forEach(f ->
                sb.append("- ")
                        .append(f.getLabel())
                        .append(": ")
                        .append(f.getValue())
                        .append("\n")
        );
    }
}