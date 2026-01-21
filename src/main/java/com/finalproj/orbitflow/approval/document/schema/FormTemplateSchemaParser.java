package com.finalproj.orbitflow.approval.document.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproj.orbitflow.approval.form.template.schema.FormTemplateSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 결재 양식(template_json)을 FormTemplateSchema 객체로 변환하는 파서 클래스.
 * <p>
 * 데이터베이스에 JSON 문자열 형태로 저장된 결재 양식 정의를
 * ObjectMapper를 이용해 FormTemplateSchema 타입으로 역직렬화한다.
 * <p>
 * 이 클래스는 파싱 책임만을 가지며,
 * 파싱 이후의 검증이나 비즈니스 로직은 다른 계층에서 처리한다.
 * <p>
 * JSON 구조가 FormTemplateSchema와 맞지 않거나 파싱에 실패할 경우
 * IllegalStateException을 발생시켜 상위 로직에서 처리하도록 한다.
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateSchemaParser
 * @since : 26. 1. 21. 수요일
 */


@Component
@RequiredArgsConstructor
public class FormTemplateSchemaParser {

    private final ObjectMapper objectMapper;

    public FormTemplateSchema parse(String json) {
        try {
            return objectMapper.readValue(json, FormTemplateSchema.class);
        } catch (Exception e) {
            throw new IllegalStateException("template_json 파싱 실패", e);
        }
    }
}