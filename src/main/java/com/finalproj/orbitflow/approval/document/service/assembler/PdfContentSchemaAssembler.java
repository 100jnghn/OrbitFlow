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
 * 결재 양식(FormTemplate)에 정의된 필드 스키마를
 * PDF 출력에 사용할 수 있는 형태로 변환하는 어셈블러 클래스.
 *
 * <p>
 * FormTemplateSchema는 화면 입력과 편집을 기준으로 한 구조이기 때문에,
 * 그대로는 PDF에 출력하기에 적합하지 않은 값들이 포함되어 있다.
 * 이 클래스는 해당 스키마를 순회하면서
 * PDF에 실제로 표시될 값만 추려 {@link PdfContentSchema}로 변환한다.
 * </p>
 *
 * <p>
 * 필드 타입에 따라 값 변환 방식이 다르며,
 * 예를 들어 사원/조직 검색 필드는 사람이 읽을 수 있는 문자열로,
 * radio, checkbox 필드는 선택된 항목의 label 값으로 치환한다.
 * divider나 notice와 같이 화면 표시용 필드는
 * PDF에서는 value를 가지지 않도록 처리한다.
 * </p>
 *
 * <p>
 * 이 클래스는 PDF 렌더링 과정에서 필요한 데이터 구조를 준비하는 역할만 담당하며,
 * 실제 HTML 생성이나 스타일, 이미지 처리 로직은
 * 이후 렌더링 단계에 위임한다.
 * </p>
 *
 * @author Choi MinHyeok
 * @filename PdfContentSchemaAssembler
 * @since 2026.01.03
 */


@Component
public class PdfContentSchemaAssembler {

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

    private PdfField convertField(FormFieldSchema field) {

        return PdfField.builder()
                .fieldType(field.getFieldType())
                .label(field.getLabel())
                .order(field.getOrder())
                .value(convertValue(field))
                .meta(convertMeta(field))
                .build();
    }


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