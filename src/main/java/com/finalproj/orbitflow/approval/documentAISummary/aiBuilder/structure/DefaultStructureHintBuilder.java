package com.finalproj.orbitflow.approval.documentAISummary.aiBuilder.structure;

import com.finalproj.orbitflow.approval.documentAISummary.service.DocumentAiSummaryService;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormFieldSchema;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DefaultStructureHintBuilder
 * @since : 26. 1. 6. 화요일
 **/


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
