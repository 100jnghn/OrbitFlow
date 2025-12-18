package com.finalproj.orbitflow.approval.formTemplate.entity;

import com.finalproj.orbitflow.approval.formTemplate.enums.AffectTag;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AffectTagListConverter
 * @since : 25. 12. 18. 목요일
 **/


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
                    new TypeReference<List<AffectTag>>() {
                    }
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("AffectTag 역직렬화 실패", e);
        }
    }
}
