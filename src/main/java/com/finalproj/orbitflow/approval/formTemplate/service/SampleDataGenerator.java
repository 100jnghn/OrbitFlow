package com.finalproj.orbitflow.approval.formTemplate.service;

import com.finalproj.orbitflow.approval.formTemplate.schema.FormFieldSchema;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * FormTemplate의 fieldType 정의를 기반으로
 * 상세/미리보기 화면에서 사용할 샘플 데이터를 생성한다.
 *
 * <p>
 * - 수정 불가 (read-only)
 * - validation / 결재 규칙 미적용
 * - 항상 동일한 결과를 반환 (deterministic)
 * </p>
 *
 * @author Choi MinHyeok
 * @since 2025.12.21
 */
@Service
public class SampleDataGenerator {

    private static final String PLACEHOLDER_IMAGE_URL = "https://placehold.co/300x200";

    /**
     * FormTemplate(Entity)를 입력으로 받아
     * fieldId → sample value 형태의 preview data를 생성한다.
     */
    public Map<String, Object> generate(FormTemplateSchema schema) {
        Map<String, Object> previewData = new LinkedHashMap<>();
        schema.getFields().stream()
                .sorted(Comparator.comparing(
                        f -> f.getOrder() == null ? Integer.MAX_VALUE : f.getOrder()
                ))
                .forEach(field ->
                        previewData.put(field.getFieldId(), generateByType(field))
                );
        return previewData;
    }


    /**
     * fieldType 기준으로 샘플 값을 생성
     */
    private Object generateByType(FormFieldSchema field) {
        String type = field.getFieldType();
        Map<String, Object> meta = field.getMeta() != null ? field.getMeta() : Collections.emptyMap();

        switch (type) {

            case "document-title":
                return meta.getOrDefault("value", "문서 제목 예시");

            case "text":
                return "텍스트 예시";

            case "textarea":
                return "여러 줄 텍스트 예시\n두 번째 줄입니다.";

            case "number":
                return 1;

            case "divider":
                return null;

            case "time":
                return "09:00";

            case "time-range":
                return Map.of(
                        "start", "09:00",
                        "end", "18:00"
                );

            case "date":
                return "2025-01-01";

            case "date-range":
            case "leave-date-range":
                return Map.of(
                        "start", "2025-01-01",
                        "end", "2025-01-05"
                );

            case "radio":
                return getFirstOptionId(meta);

            case "checkbox":
                String optionId = getFirstOptionId(meta);
                return optionId != null
                        ? List.of(optionId)
                        : Collections.emptyList();

            case "notice":
                return meta.getOrDefault("message", "안내 문구 예시");

            case "table":
                return generateTable(meta);

            case "image":
                return Map.of(
                        "src", PLACEHOLDER_IMAGE_URL,
                        "alt", meta.getOrDefault("alt", "이미지 미리보기")
                );

            case "currency":
                return 100_000;

            case "address":
                return "서울특별시 강남구 테헤란로";

            case "employee-search":
                return generateSearchValue(meta, "EMP001", "홍길동");

            case "department-search":
                return generateSearchValue(meta, "DEPT001", "개발팀");

            default:
                return null;
        }
    }

    /**
     * table field의 샘플 row 생성
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> generateTable(Map<String, Object> meta) {
        List<Map<String, Object>> rows = new ArrayList<>();

        Map<String, Object> rowPolicy =
                (Map<String, Object>) meta.get("rowPolicy");

        int minRows = 1;
        if (rowPolicy != null && rowPolicy.get("min") instanceof Number) {
            minRows = ((Number) rowPolicy.get("min")).intValue();
        }

        List<Map<String, Object>> columns =
                (List<Map<String, Object>>) meta.get("columns");

        for (int i = 0; i < minRows; i++) {
            Map<String, Object> row = new LinkedHashMap<>();

            if (columns != null) {
                for (Map<String, Object> column : columns) {
                    String columnId = (String) column.get("id");
                    String columnType = (String) column.get("type");
                    row.put(columnId, generateTableColumnValue(columnType));
                }
            }

            rows.add(row);
        }

        return rows;
    }

    /**
     * table column 타입별 샘플 값
     */
    private Object generateTableColumnValue(String type) {
        if (type == null) {
            return null;
        }

        switch (type) {
            case "text":
                return "텍스트";
            case "number":
                return 1;
            case "currency":
                return 10_000;
            default:
                return null;
        }
    }

    /**
     * radio / checkbox 첫 번째 옵션 id 반환
     */
    @SuppressWarnings("unchecked")
    private String getFirstOptionId(Map<String, Object> meta) {
        Object optionsObj = meta.get("options");

        if (!(optionsObj instanceof List)) {
            return null;
        }

        List<Map<String, Object>> options =
                (List<Map<String, Object>>) optionsObj;

        if (options.isEmpty()) {
            return null;
        }

        return (String) options.get(0).get("id");
    }

    /**
     * employee-search / department-search 샘플 값 생성
     */
    private Object generateSearchValue(
            Map<String, Object> meta,
            String id,
            String name
    ) {
        boolean multiple = Boolean.TRUE.equals(meta.get("multiple"));

        Map<String, Object> value = Map.of(
                "id", id,
                "name", name
        );

        return multiple ? List.of(value) : value;
    }
}
