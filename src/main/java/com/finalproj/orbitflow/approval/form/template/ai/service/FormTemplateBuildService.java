package com.finalproj.orbitflow.approval.form.template.ai.service;

import com.finalproj.orbitflow.approval.form.template.ai.dto.AiFormComponent;
import com.finalproj.orbitflow.approval.form.template.ai.dto.AiFormDesignResult;
import com.finalproj.orbitflow.approval.form.template.ai.dto.FormField;
import com.finalproj.orbitflow.approval.form.template.ai.dto.FormTemplateJson;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * AI가 설계한 컴포넌트 결과를
 * 실제 문서 폼에서 사용할 수 있는 필드 구조로 변환하는 서비스이다.
 * <p>
 * 이 클래스는 AI가 반환한 설계 결과(AiFormDesignResult)를 그대로 저장하지 않고,
 * 시스템에서 사용하는 FormTemplateJson 구조로 한 번 더 가공하는 역할을 맡는다.
 * <p>
 * 처리 과정에서는 다음과 같은 작업을 수행한다.
 * - 문서 제목 필드를 항상 최상단에 자동 추가
 * - AI가 설계한 각 컴포넌트를 FormField 단위로 변환
 * - fieldId, optionId, order 등 시스템 내부 식별자 생성
 * - 컴포넌트 타입별 기본 meta 값 보정 및 구조 정규화
 * <p>
 * 이 단계에서는 정책 판단이나 의미 검증은 수행하지 않으며,
 * 해당 책임은 앞선 Processor 파이프라인 단계에서 이미 완료되었다고 가정한다.
 * 여기서는 오직 “사용 가능한 폼 구조로 만드는 것”에만 집중한다.
 * <p>
 * 즉, 이 서비스는
 * AI 설계 결과와 실제 폼 렌더링/저장 구조 사이를 연결하는
 * 마지막 변환 단계 역할을 수행한다.
 *
 * @author Choi MinHyeok
 * @filename FormTemplateBuildService
 * @since 2026. 1. 8.
 */


@Service
public class FormTemplateBuildService {

    public FormTemplateJson build(
            AiFormDesignResult aiResult,
            String formName
    ) {
        List<FormField> fields = new ArrayList<>();

        fields.add(createDocumentTitleField(formName));

        int order = 2;
        for (AiFormComponent component : aiResult.components()) {
            FormField field = convert(component, order++);
            fields.add(field);
        }

        return new FormTemplateJson(fields);
    }


    private FormField createDocumentTitleField(String formName) {
        return FormField.builder()
                .fieldId("document-title")
                .fieldType("document-title")
                .label("문서 제목")
                .order(1)
                .required(true)
                .meta(Map.of(
                        "value", formName,
                        "maxLength", 100,
                        "placeholder", "문서 제목을 입력하세요."
                ))
                .build();
    }


    private FormField convert(AiFormComponent component, int order) {
        return FormField.builder()
                .fieldId(generateFieldId())
                .fieldType(component.type())
                .label(component.label())
                .required(Boolean.TRUE.equals(component.required()))
                .order(order)
                .meta(buildMeta(component.type(), component.meta()))
                .build();
    }

    private String generateFieldId() {
        return "comp_" +
                Long.toString(ThreadLocalRandom.current().nextLong(), 36)
                + "_" + System.currentTimeMillis();
    }

    private String generateOptionId() {
        return "opt_" +
                Long.toString(ThreadLocalRandom.current().nextLong(), 36)
                + "_" + System.currentTimeMillis();
    }


    private Map<String, Object> buildMeta(String type, Map<String, Object> meta) {
        Map<String, Object> m = meta == null ? new HashMap<>() : new HashMap<>(meta);

        switch (type) {

            case "text":
            case "textarea":
                m.putIfAbsent("placeholder", "");
                break;

            case "number":
                break;

            case "date":
            case "time":
                m.putIfAbsent("format", "");
                break;

            case "date-range":
            case "time-range":
                m.putIfAbsent("startLabel", "");
                m.putIfAbsent("endLabel", "");
                break;

            case "radio":
            case "checkbox":
                Object raw = m.get("options");
                List<Map<String, Object>> options = new ArrayList<>();

                if (raw instanceof List<?>) {
                    for (Object o : (List<?>) raw) {
                        if (o == null) continue;

                        Map<String, Object> opt = new HashMap<>();
                        opt.put("id", generateOptionId());
                        opt.put("label", String.valueOf(o));
                        options.add(opt);
                    }
                }

                m.put("options", options);
                break;


            case "notice":
                m.putIfAbsent("message", "");
                m.putIfAbsent("style", "info");
                break;

            case "table":
                List<Map<String, Object>> cols = new ArrayList<>();
                Object rawCols = m.get("columns");

                if (rawCols instanceof List<?>) {
                    for (Object c : (List<?>) rawCols) {
                        if (!(c instanceof Map<?, ?> col)) continue;

                        Map<String, Object> colMap = new HashMap<>();
                        colMap.put("id", generateFieldId());
                        colMap.put("type", col.get("type"));
                        colMap.put("label", col.get("label"));
                        colMap.put("required", Boolean.TRUE.equals(col.get("required")));
                        colMap.put("meta",
                                col.get("meta") instanceof Map ? col.get("meta") : new HashMap<>());

                        cols.add(colMap);
                    }
                }

                m.put("columns", cols);

                if (!(m.get("rowPolicy") instanceof Map)) {
                    Map<String, Object> rowPolicy = new HashMap<>();
                    rowPolicy.put("min", 1);
                    rowPolicy.put("addable", true);
                    rowPolicy.put("removable", true);
                    m.put("rowPolicy", rowPolicy);
                }

                break;


            case "image":
                m.putIfAbsent("maxCount", 1);
                m.putIfAbsent("value", new ArrayList<>());
                break;


            case "currency":
                m.putIfAbsent("unit", "KRW");
                m.putIfAbsent("locale", "");
                break;

            case "address":
                m.putIfAbsent("usePostcodeApi", false);
                break;

            case "employee-search":
            case "department-search":
                m.putIfAbsent("multiple", false);
                break;

            case "event-date-range":
                m.putIfAbsent("startLabel", "시작일");
                m.putIfAbsent("endLabel", "종료일");
                break;

            case "divider":
            default:
                break;
        }

        return m;
    }

}
