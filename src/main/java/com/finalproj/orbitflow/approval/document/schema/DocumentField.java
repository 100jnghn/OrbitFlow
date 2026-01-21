package com.finalproj.orbitflow.approval.document.schema;

import java.util.Map;

/**
 * 문서 내용(JSON)에서 하나의 필드를 표현하기 위한 경량 객체.
 * <p>
 * DocumentContent에 저장된 JSON 구조 중,
 * 개별 필드(fieldId, fieldType, label, value)를
 * 코드 레벨에서 다루기 쉬운 형태로 묶기 위해 사용된다.
 * <p>
 * 이 객체는 엔티티나 도메인 모델이 아니라,
 * 문서 내용을 파싱하는 과정에서 일시적으로 사용되는 전달용 객체이며
 * 비즈니스 로직을 포함하지 않는다.
 * <p>
 * 주로 DocumentContentParser에서
 * 특정 타입의 필드를 추출한 뒤 후속 처리 로직으로 전달하는 용도로 사용된다.
 *
 * @author : Choi MinHyeok
 * @filename : DocumentField
 * @since : 25. 12. 31. 수요일
 */


public record DocumentField(
        String fieldId,
        String fieldType,
        String label,
        Map<String, Object> value)
{
}
