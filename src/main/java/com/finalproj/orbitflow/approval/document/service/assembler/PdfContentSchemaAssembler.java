package com.finalproj.orbitflow.approval.document.service.assembler;

import com.finalproj.orbitflow.approval.document.schema.PdfField;
import com.finalproj.orbitflow.approval.document.schema.PdfContentSchema;
import com.finalproj.orbitflow.approval.form.template.schema.FormFieldSchema;
import com.finalproj.orbitflow.approval.form.template.schema.FormTemplateSchema;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : PdfContentSchemaAssembler
 * @since : 26. 1. 3. 토요일
 **/


@Component
public class PdfContentSchemaAssembler {

    /**
     * FormTemplateSchema → PdfContentSchema 변환
     */
    public PdfContentSchema from(FormTemplateSchema schema) {

        if (schema == null || schema.getFields() == null) {
            return PdfContentSchema.builder()
                    .fields(List.of())
                    .build();
        }

        List<PdfField> fields = schema.getFields().stream()
                .sorted(Comparator.comparingInt(FormFieldSchema::getOrder))
                .map(this::convertField)
                .toList();

        return PdfContentSchema.builder()
                .fields(fields)
                .build();
    }

    /**
     * 단일 필드 변환
     */
    private PdfField convertField(FormFieldSchema field) {

        return PdfField.builder()
                .fieldType(field.getFieldType())
                .label(field.getLabel())
                .order(field.getOrder())
                .value(convertValue(field))
                .meta(convertMeta(field))
                .build();
    }

    /**
     * ===============================
     * value 변환 (PDF 핵심 로직)
     * ===============================
     */
    private Object convertValue(FormFieldSchema field) {

        Object value = field.getValue();
        if (value == null) return null;

        return switch (field.getFieldType()) {

            /* ===== 단순 값 ===== */
            case "text", "textarea", "number", "currency",
                 "date", "time" -> value;

            /* ===== 범위 / 복합 ===== */
            case "time-range", "date-range", "event-date-range" -> value;

            /* ===== 조직 검색 ===== */
            case "department-search" -> convertDepartment(value);

            /* ===== 사원 검색 ===== */
            case "employee-search" -> convertEmployee(value);

            /* ===== 선택 ===== */
            case "radio" -> convertRadio(field, value);

            case "checkbox" -> convertCheckbox(field, value);

            /* ===== 이미지 ===== */
            case "image" -> convertImages(value);

            /* ===== 테이블 ===== */
            case "table" -> value;

            /* ===== 표시만 하는 필드 ===== */
            case "divider", "notice" -> null;

            default -> value;
        };
    }

    /**
     * ===============================
     * meta 변환 (PDF에 필요한 것만)
     * ===============================
     */
    private Map<String, Object> convertMeta(FormFieldSchema field) {

        if (field.getMeta() == null) {
            return Map.of();
        }

        return switch (field.getFieldType()) {

            case "table",
                 "notice",
                 "event-date-range" -> field.getMeta();

            default -> Map.of();
        };
    }

    /* =========================================================
       개별 타입 변환 helper
    ========================================================= */

    private String convertDepartment(Object value) {
        if (!(value instanceof Map<?, ?> map)) return "-";

        Object name = map.get("name");
        return name != null ? String.valueOf(name) : "-";
    }

    private String convertEmployee(Object value) {
        if (!(value instanceof Map<?, ?> map)) return "-";

        String dept = (String) map.get("departmentName");
        String name = (String) map.get("name");
        String empNo = (String) map.get("employeeNo");

        return Stream.of(dept, name, empNo)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" / "));
    }

    private String convertRadio(FormFieldSchema field, Object value) {

        if (!(value instanceof String selectedId)) return "-";
        if (field.getMeta() == null) return "-";

        Object optionsObj = field.getMeta().get("options");
        if (!(optionsObj instanceof List<?> options)) return "-";

        return options.stream()
                .filter(o -> o instanceof Map)
                .map(o -> (Map<?, ?>) o)
                .filter(m -> selectedId.equals(m.get("id")))
                .map(m -> (String) m.get("label"))
                .findFirst()
                .orElse("-");
    }

    private String convertCheckbox(FormFieldSchema field, Object value) {

        if (value == null) return "-";

        // 이미 문자열이면 그대로 (detail / PDF 기준)
        if (value instanceof String s) {
            return s.isBlank() ? "-" : s;
        }

        if (!(value instanceof List<?> selectedIds)) return "-";
        if (field.getMeta() == null) return "-";

        Object optionsObj = field.getMeta().get("options");
        if (!(optionsObj instanceof List<?> options)) return "-";

        Set<String> idSet = selectedIds.stream()
                .map(String::valueOf)
                .collect(Collectors.toSet());

        String result = options.stream()
                .filter(o -> o instanceof Map)
                .map(o -> (Map<?, ?>) o)
                .filter(m -> idSet.contains(String.valueOf(m.get("id"))))
                .map(m -> (String) m.get("label"))
                .collect(Collectors.joining(", "));

        return result.isBlank() ? "-" : result;
    }


    /**
     * 이미지 → documentFileId 리스트
     */
    private List<Map<String, Object>> convertImages(Object value) {

        if (!(value instanceof List<?> list)) return List.of();

        return list.stream()
                .filter(v -> v instanceof Map<?, ?>)
                .map(v -> {
                    Map<?, ?> m = (Map<?, ?>) v;

                    Map<String, Object> copy = new HashMap<>();
                    for (Map.Entry<?, ?> e : m.entrySet()) {
                        if (e.getKey() == null) continue;
                        copy.put(String.valueOf(e.getKey()), e.getValue());
                    }
                    return copy;
                })
                .toList();
    }


}