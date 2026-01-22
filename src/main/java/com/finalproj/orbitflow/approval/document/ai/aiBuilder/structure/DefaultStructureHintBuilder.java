package com.finalproj.orbitflow.approval.document.ai.aiBuilder.structure;

import com.finalproj.orbitflow.approval.document.ai.service.DocumentAiSummaryService;
import com.finalproj.orbitflow.approval.form.template.schema.FormFieldSchema;
import com.finalproj.orbitflow.approval.form.template.schema.FormTemplateSchema;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 기본 문서 구조 힌트 생성을 담당하는 StructureHintBuilder 구현체.
 * <p>
 * 결재 양식의 FormTemplateSchema를 분석하여,
 * 문서가 어떤 형태와 성격을 가지는지에 대한
 * 구조적 힌트 문장을 생성한다.
 * <p>
 * 필드 타입, 컬럼 구성, 라벨명 등을 기준으로
 * 텍스트 중심 문서, 표 기반 문서, 일정/금액/조직 정보 포함 여부 등을 판단하며,
 * AI가 문서를 올바르게 해석할 수 있도록 보조적인 설명을 제공한다.
 * <p>
 * 생성된 구조 힌트는 요약이나 변경 분석의 결과를 직접 만들기 위한 것이 아니라,
 * 프롬프트 내에서 문서의 전반적인 맥락과 형태를 이해시키기 위한 참고 정보로 사용된다.
 * <p>
 * 과도한 힌트 생성을 방지하기 위해 중복 문장은 제거되며,
 * 최대 5개의 핵심 힌트만 반환하도록 제한되어 있다.
 *
 * @author : Choi MinHyeok
 * @filename : DefaultStructureHintBuilder
 * @since : 26. 1. 6. 화요일
 */


@Component
public class DefaultStructureHintBuilder implements StructureHintBuilder {

    @Override
    public String build(FormTemplateSchema templateSchema) {

        List<FormFieldSchema> fields =
                templateSchema.getFields().stream()
                        .filter(DocumentAiSummaryService::hasMeaningfulValue)
                        .toList();

        Set<String> hints = new LinkedHashSet<>();

        addTextHints(fields, hints);
        addTableHints(fields, hints);
        addDateHints(fields, hints);
        addNumberHints(fields, hints);
        addEmployeeOrgHints(fields, hints);
        addLabelBasedHints(fields, hints);

        return hints.stream()
                .limit(5)
                .collect(Collectors.joining("\n"));
    }


    private void addTextHints(List<FormFieldSchema> fields, Set<String> hints) {
        long count = fields.stream()
                .filter(f -> Set.of(
                        "document-title",
                        "text",
                        "textarea",
                        "notice"
                ).contains(f.getFieldType()))
                .count();

        if (count >= 2) {
            hints.add("여러 개의 텍스트 기반 섹션을 포함하고 있다.");
        }
    }


    @SuppressWarnings("unchecked")
    private List<String> extractColumnLabels(FormFieldSchema field) {
        if (field.getMeta() == null) return List.of();

        Object columnsObj = field.getMeta().get("columns");
        if (!(columnsObj instanceof List<?> columns)) return List.of();

        return columns.stream()
                .filter(c -> c instanceof Map)
                .map(c -> (Map<String, Object>) c)
                .map(m -> m.get("label"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .toList();
    }


    private void addTableHints(List<FormFieldSchema> fields, Set<String> hints) {
        for (FormFieldSchema field : fields) {
            if (!"table".equals(field.getFieldType())) continue;

            List<String> columnLabels = extractColumnLabels(field);

            if (columnLabels.isEmpty()) {
                hints.add("표 형식으로 정리된 구조화된 항목이 포함되어 있다.");
                return;
            }

            if (isComparisonTable(columnLabels)) {
                hints.add("동일한 항목에 대해 서로 다른 상태나 시점을 비교할 수 있는 표 형식의 내용이 포함되어 있다.");
            } else if (isAttributeTable(columnLabels)) {
                hints.add("여러 항목의 구성이나 속성을 정리한 표 형식의 내용이 포함되어 있다.");
            } else if (isKeyValueTable(columnLabels)) {
                hints.add("항목과 그에 대한 값을 대응하여 정리한 표 형식의 내용이 포함되어 있다.");
            } else {
                hints.add("여러 항목을 목록 형태로 정리한 표 형식의 내용이 포함되어 있다.");
            }
            return;
        }
    }


    private boolean isComparisonTable(List<String> columns) {
        return containsAny(columns, List.of("현재", "기존", "전", "AS-IS", "Before"))
                && containsAny(columns, List.of("변경", "이후", "후", "개선", "TO-BE", "After"));
    }

    private boolean isAttributeTable(List<String> columns) {
        return containsAny(columns, List.of("항목", "구분", "유형", "종류", "속성"));
    }

    private boolean isKeyValueTable(List<String> columns) {
        return columns.size() == 2
                && containsAny(columns, List.of("값", "내용", "설명", "상세", "비고"));
    }

    private boolean containsAny(List<String> columns, List<String> keywords) {
        return columns.stream()
                .anyMatch(c -> keywords.stream().anyMatch(c::contains));
    }

    private void addDateHints(List<FormFieldSchema> fields, Set<String> hints) {
        boolean hasRange = fields.stream()
                .anyMatch(f -> Set.of(
                        "date-range",
                        "event-date-range",
                        "time-range"
                ).contains(f.getFieldType()));

        boolean hasPoint = fields.stream()
                .anyMatch(f -> Set.of("date", "time").contains(f.getFieldType()));

        if (hasRange) {
            hints.add("시작과 종료 시점을 포함하는 일정 정보가 포함되어 있다.");
        } else if (hasPoint) {
            hints.add("특정 시점과 관련된 일정 정보가 포함되어 있다.");
        }
    }

    private void addNumberHints(List<FormFieldSchema> fields, Set<String> hints) {
        if (fields.stream().anyMatch(f ->
                Set.of("number", "currency").contains(f.getFieldType()))) {
            hints.add("수치 또는 금액과 관련된 정보가 포함되어 있다.");
        }
    }

    private void addEmployeeOrgHints(List<FormFieldSchema> fields, Set<String> hints) {
        if (fields.stream().anyMatch(f ->
                Set.of("employee-search", "department-search").contains(f.getFieldType()))) {
            hints.add("문서와 관련된 인원 또는 조직 정보를 지정하는 항목이 포함되어 있다.");
        }
    }


    private void addLabelBasedHints(List<FormFieldSchema> fields, Set<String> hints) {
        for (FormFieldSchema field : fields) {
            String label = field.getLabel();
            if (label == null) continue;

            if (label.contains("문제")) {
                hints.add("문제점 또는 이슈를 설명하는 항목이 포함되어 있다.");
            } else if (label.contains("일정") || label.contains("기간")) {
                hints.add("일정이나 기간과 관련된 항목이 포함되어 있다.");
            } else if (label.contains("비용") || label.contains("금액")) {
                hints.add("비용 또는 금액 정보를 설명하는 항목이 포함되어 있다.");
            }
        }
    }

}
