package com.finalproj.orbitflow.approval.form.template.entity;

import com.finalproj.orbitflow.approval.form.template.enums.AffectTag;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * FormTemplate 엔티티의 AffectTag 목록을
 * 데이터베이스에 JSON 문자열 형태로 저장하고,
 * 조회 시 다시 AffectTag 리스트로 변환하기 위한 JPA AttributeConverter이다.
 * <p>
 * 결재 양식에는 근태, 일정 등과 연동되는 영향 정보가 여러 개 존재할 수 있기 때문에,
 * 이를 별도 테이블로 분리하지 않고 JSON 컬럼 형태로 관리하도록 설계하였다.
 * <p>
 * 이 컨버터는
 * - 엔티티 → DB 저장 시: AffectTag 리스트를 JSON 문자열로 직렬화하고
 * - DB 조회 시: JSON 문자열을 AffectTag 리스트로 역직렬화한다.
 * <p>
 * DB 값이 null 또는 비어 있는 경우에는
 * 엔티티에서는 빈 리스트로 처리하여
 * 이후 로직에서 null 체크 부담을 줄이도록 한다.
 * <p>
 * 직렬화 또는 역직렬화 과정에서 오류가 발생하면,
 * 잘못된 데이터 상태를 조기에 인지할 수 있도록 예외를 발생시킨다.
 *
 * @author Choi MinHyeok
 * @filename AffectTagListConverter
 * @since 2025. 12. 18.
 */


@Converter
public class AffectTagListConverter
        implements AttributeConverter<List<AffectTag>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<AffectTag> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null; // 또는 "[]"
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("AffectTag 직렬화 실패", e);
        }
    }

    @Override
    public List<AffectTag> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(
                    dbData,
                    new TypeReference<>() {
                    }
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("AffectTag 역직렬화 실패", e);
        }
    }
}
