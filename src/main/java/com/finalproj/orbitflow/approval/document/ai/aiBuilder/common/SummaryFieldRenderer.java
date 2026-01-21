package com.finalproj.orbitflow.approval.document.ai.aiBuilder.common;

import com.finalproj.orbitflow.approval.document.ai.dto.AiSummaryField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI 문서 요약 프롬프트 생성을 위한 필드 렌더링 유틸리티.
 *
 * <p>
 * {@link AiSummaryField} 목록을 사람이 읽기 쉬운 텍스트 형태로 변환하여
 * AI 프롬프트에 삽입하는 역할을 담당한다.
 * </p>
 *
 * <p>
 * 필드 타입에 따라 렌더링 방식을 분기하며,
 * 일반 텍스트/숫자/날짜 필드는 단순 항목 형태로,
 * 테이블(table) 타입 필드는 행 단위 요약 형태로 변환한다.
 * </p>
 *
 * <p>
 * 본 클래스는 상태를 가지지 않는 순수 유틸리티 클래스로 설계되었으며,
 * 프롬프트 구성 로직과 AI 호출 로직으로부터 독립적으로
 * 요약 표현 규칙만을 책임진다.
 * </p>
 *
 * <p>
 * 렌더링 결과는 AI 모델이 문서 구조와 내용을
 * 명확히 이해할 수 있도록 일관된 포맷을 유지한다.
 * </p>
 *
 * @author Choi MinHyeok
 * @fileName SummaryFieldRenderer
 * @since 2026.01.06
 */


public class SummaryFieldRenderer {

    private SummaryFieldRenderer() {
        // util class
    }

    public static void appendFields(
            StringBuilder sb,
            String title,
            List<AiSummaryField> fields
    ) {
        if (fields == null || fields.isEmpty()) {
            return;
        }

        sb.append(title).append("\n");

        for (AiSummaryField field : fields) {

            // table 분기
            if ("table".equals(field.getFieldType())) {
                appendTableField(sb, field);
                continue;
            }

            // text / number / date 등
            sb.append("- ")
                    .append(field.getLabel())
                    .append(": ")
                    .append(field.getValue())
                    .append("\n");
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map.Entry<String, String>> extractColumnIdAndLabels(
            Map<String, Object> meta
    ) {
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
    private static void appendTableField(
            StringBuilder sb,
            AiSummaryField field
    ) {
        sb.append("- ").append(field.getLabel()).append(":\n");

        List<Map.Entry<String, String>> columns =
                extractColumnIdAndLabels(field.getMeta());

        Object value = field.getValue();
        if (!(value instanceof List<?> rows) || rows.isEmpty()) {
            sb.append("  - (입력된 항목 없음)\n");
            return;
        }

        for (Object r : rows) {
            if (!(r instanceof Map<?, ?> row)) continue;

            List<String> parts = new ArrayList<>();

            for (Map.Entry<String, String> col : columns) {
                Object v = row.get(col.getKey());
                parts.add(col.getValue() + ": " + (v != null ? v : ""));
            }

            sb.append("  - ")
                    .append(String.join(", ", parts))
                    .append("\n");
        }
    }
}