package com.finalproj.orbitflow.approval.document.ai.aiBuilder.common;

import com.finalproj.orbitflow.approval.document.ai.dto.AiSummaryField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : SummaryFieldRenderer
 * @since : 26. 1. 6. 화요일
 **/


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