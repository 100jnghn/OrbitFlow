package com.finalproj.orbitflow.approval.documentAISummary.aiBuilder;

import com.finalproj.orbitflow.approval.documentAISummary.dto.AiSummaryField;
import com.finalproj.orbitflow.approval.documentAISummary.dto.AiSummaryReqDto;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AiSummaryPromptBuilder
 * @since : 26. 1. 5. 월요일
 **/


@Component
@RequiredArgsConstructor
public class AiSummaryPromptBuilder {

    private final StructureHintBuilder structureHintBuilder;


    public String build(AiSummaryReqDto request, FormTemplateSchema templateSchema) {

        StringBuilder sb = new StringBuilder();

        // BASE PROMPT
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
                - 요약의 첫 문장은 문서 제목을 반영하여 공식적인 보고서 문장으로 작성할 것
                
                """);

        // STRUCTURE HINT
        String structureHint = structureHintBuilder.build(templateSchema);
        if (!structureHint.isBlank()) {
            sb.append("""
                    [문서 구조 정보]
                    """);
            sb.append(structureHint).append("\n\n");
        }

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

        // 첨부파일
        appendAttachments(sb, request.getAttachmentNames());

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

        for (AiSummaryField field : fields) {

            // ✅ table 분기
            if ("table".equals(field.getFieldType())) {
                appendTableField(sb, field);
                continue;
            }

            // ✅ 기존 text / number / date 등
            sb.append("- ")
                    .append(field.getLabel())
                    .append(": ")
                    .append(field.getValue())
                    .append("\n");
        }
    }


    @SuppressWarnings("unchecked")
    private List<Map.Entry<String, String>> extractColumnIdAndLabels(Map<String, Object> meta) {
        if (meta == null) return List.of();

        Object columnsObj = meta.get("columns");
        if (!(columnsObj instanceof List<?> columns)) return List.of();

        List<Map.Entry<String, String>> result = new ArrayList<>();

        for (Object c : columns) {
            if (!(c instanceof Map<?, ?> col)) continue;

            Object id = col.get("id");
            Object label = col.get("label");

            if (id != null && label != null) {
                result.add(Map.entry(id.toString(), label.toString()));
            }
        }
        return result;
    }


    @SuppressWarnings("unchecked")
    private void appendTableField(StringBuilder sb, AiSummaryField field) {
        sb.append("- ").append(field.getLabel()).append(":\n");

        List<Map.Entry<String, String>> columns =
                extractColumnIdAndLabels(field.getMeta());

        List<Map<String, Object>> rows =
                (List<Map<String, Object>>) field.getValue();

        if (rows == null || rows.isEmpty()) {
            sb.append("  - (입력된 항목 없음)\n");
            return;
        }

        for (Map<String, Object> row : rows) {
            List<String> parts = new ArrayList<>();

            for (Map.Entry<String, String> col : columns) {
                Object value = row.get(col.getKey());
                parts.add(col.getValue() + ": " + (value != null ? value : ""));
            }

            sb.append("  - ")
                    .append(String.join(", ", parts))
                    .append("\n");
        }
    }


    private void appendAttachments(StringBuilder sb, List<String> attachmentNames) {
        if (attachmentNames == null || attachmentNames.isEmpty()) {
            return;
        }

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
}