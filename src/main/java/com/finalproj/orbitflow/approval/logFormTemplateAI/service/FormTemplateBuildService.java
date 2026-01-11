package com.finalproj.orbitflow.approval.logFormTemplateAI.service;

import com.finalproj.orbitflow.approval.logFormTemplateAI.dto.AiFormComponent;
import com.finalproj.orbitflow.approval.logFormTemplateAI.dto.AiFormDesignResult;
import com.finalproj.orbitflow.approval.logFormTemplateAI.dto.FormField;
import com.finalproj.orbitflow.approval.logFormTemplateAI.dto.FormTemplateJson;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateBuildService
 * @since : 26. 1. 8. 목요일
 **/

@Service
public class FormTemplateBuildService {

    public FormTemplateJson build(
            AiFormDesignResult aiResult,
            String formName
    ) {
        List<FormField> fields = new ArrayList<>();

        fields.add(createDocumentTitleField(formName));

        int order = 2; // ⬅️ 2번부터 시작
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
                // maxLength는 선택
                break;

            case "number":
                // min, max 선택
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
                // ⚠️ 정책은 여기서 판단하지 않음
                m.putIfAbsent("startLabel", "시작일");
                m.putIfAbsent("endLabel", "종료일");
                // baseRole / affect / ui 는 processor 단계에서 이미 세팅됐다고 가정
                break;

            case "divider":
            default:
                // meta 없음
                break;
        }

        return m;
    }

}
