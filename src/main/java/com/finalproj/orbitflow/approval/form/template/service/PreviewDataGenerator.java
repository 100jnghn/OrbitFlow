package com.finalproj.orbitflow.approval.form.template.service;

import com.finalproj.orbitflow.approval.form.template.schema.FormTemplateSchema;

import java.util.Map;

/**
 * FormTemplate 기반으로 미리보기/상세 화면에 사용할
 * 샘플 데이터를 생성하는 Generator 인터페이스
 *
 * @author : Choi MinHyeok
 * @filename : PreviewDataGenerator
 * @since : 25. 12. 21. 일요일
 **/


/**
 * Preview 데이터 생성 전략 인터페이스
 * <p>
 * TODO:
 * - 실제 문서 미리보기 기능 도입 시
 *   Document 기반 Generator로 확장 예정
 */
public interface PreviewDataGenerator {
    Map<String, Object> generate(FormTemplateSchema schema);
}

