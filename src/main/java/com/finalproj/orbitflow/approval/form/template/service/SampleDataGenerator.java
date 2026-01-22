package com.finalproj.orbitflow.approval.form.template.service;

import com.finalproj.orbitflow.approval.form.template.schema.FormFieldSchema;
import com.finalproj.orbitflow.approval.form.template.schema.FormTemplateSchema;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 결재 양식(FormTemplate)의 구조 정의를 기반으로,
 * 상세 조회 및 미리보기 화면에서 사용할 샘플 데이터를 생성하는 서비스 클래스이다.
 * <p>
 * 이 클래스는 실제 사용자 입력이나 검증 로직과는 무관하게,
 * 화면 렌더링을 위한 예시 값만을 채워 넣는 역할을 수행한다.
 * <p>
 * 주요 특징은 다음과 같다.
 * - 읽기 전용(read-only) 용도로만 사용된다.
 * - validation, 결재 규칙, 상태 제약은 적용하지 않는다.
 * - 동일한 입력(schema)에 대해 항상 동일한 결과를 반환한다.
 * <p>
 * fieldType을 기준으로 미리 정의된 예시 값을 생성하며,
 * 테이블, 선택형 컴포넌트, 이벤트 컴포넌트 등
 * 복합 구조에 대해서도 최소 표시가 가능하도록 샘플 값을 구성한다.
 * <p>
 * 이 서비스는 실제 데이터 저장이나 비즈니스 판단에 관여하지 않으며,
 * 오직 미리보기 및 상세 화면 표시 품질을 보조하기 위한 용도로 사용된다.
 *
 * @author Choi MinHyeok
 * @filename SampleDataGenerator
 * @since 2025. 12. 21.
 */


@Service
public class SampleDataGenerator {

    private static final String PLACEHOLDER_IMAGE_URL =
            "https://placehold.co/600x400?text=Preview+Image";

    public void fillValues(FormTemplateSchema schema) {
        schema.getFields().forEach(field ->
                field.setValue(generateByType(field))
        );
    }

    private Object generateByType(FormFieldSchema field) {
        String type = field.getFieldType();
        Map<String, Object> meta =
                field.getMeta() != null ? field.getMeta() : Map.of();

        switch (type) {
            case "document-title":
                return "문서 제목 예시";

            case "text":
                return "텍스트 예시";

            case "textarea":
                return "여러 줄 텍스트 예시\n두 번째 줄입니다.";

            case "number":
                return 123;

            case "currency":
                return 100_000;

            case "time":
                return "09:00";

            case "date":
                return "2026-01-01";

            case "time-range":
                return Map.of(
                        "start", "09:00",
                        "end", "18:00"
                );

            case "date-range":
                return Map.of(
                        "start", "2026-01-01",
                        "end", "2026-01-03"
                );

            case "radio":
                return firstOptionId(meta);

            case "checkbox": {
                String id = firstOptionId(meta);
                return id == null ? List.of() : List.of(id);
            }

            case "event-date-range":
                return Map.of(
                        "start", "2026-01-01",
                        "end", "2026-01-03",
                        "reason", "미리보기용 사유 예시",
                        "vacationTypeId", "1"
                );

            case "table":
                return generateTable(meta);

            case "image":
                return List.of(
                        Map.of("url", PLACEHOLDER_IMAGE_URL)
                );

            case "address":
                return Map.of(
                        "postcode", "12345",
                        "roadAddress", "서울시 강남구 테헤란로",
                        "detailAddress", "101호"
                );

            case "employee-search":
                return Map.of(
                        "id", 1L,
                        "name", "홍길동",
                        "employeeNo", "OF-001",
                        "displayText", "(OF-001) 홍길동"
                );

            case "department-search":
                return Map.of(
                        "id", 1L,
                        "name", "개발팀"
                );

            case "divider":
            case "notice":
                return null;

            default:
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    private String firstOptionId(Map<String, Object> meta) {
        Object optionsObj = meta.get("options");
        if (!(optionsObj instanceof List<?> options) || options.isEmpty()) {
            return null;
        }
        Object id = ((Map<String, Object>) options.get(0)).get("id");
        return id != null ? id.toString() : null;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> generateTable(Map<String, Object> meta) {
        List<Map<String, Object>> rows = new ArrayList<>();

        int minRows = 1;
        Object rowPolicyObj = meta.get("rowPolicy");
        if (rowPolicyObj instanceof Map<?, ?> policy) {
            Object min = policy.get("min");
            if (min instanceof Number n) minRows = n.intValue();
        }

        List<Map<String, Object>> columns =
                (List<Map<String, Object>>) meta.get("columns");

        for (int i = 0; i < minRows; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            if (columns != null) {
                for (Map<String, Object> col : columns) {
                    String colId = (String) col.get("id");
                    String colType = (String) col.get("type");
                    row.put(colId, sampleByColumnType(colType));
                }
            }
            rows.add(row);
        }
        return rows;
    }

    private Object sampleByColumnType(String type) {
        if (type == null) return null;
        return switch (type) {
            case "text" -> "텍스트";
            case "number" -> 1;
            case "currency" -> 10_000;
            default -> null;
        };
    }
}
