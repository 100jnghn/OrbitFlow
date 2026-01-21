package com.finalproj.orbitflow.approval.document.schema;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : PdfField
 * @since : 26. 1. 3. 토요일
 **/


@Getter
@Builder
public class PdfField {

    private final String fieldId;
    private final String fieldType;
    private final String label;
    private final int order;
    private final Object value;
    private final Map<String, Object> meta;

    public PdfField(
            String fieldId,
            String fieldType,
            String label,
            int order,
            Object value,
            Map<String, Object> meta
    ) {
        this.fieldId = fieldId;
        this.fieldType = fieldType;
        this.label = label;
        this.order = order;
        this.value = value;
        this.meta = meta;
    }

    public String getMetaValue(String key) {
        if (meta == null) {
            return null;
        }
        Object value = meta.get(key);
        return value != null ? String.valueOf(value) : null;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getMetaMap(String key) {
        if (meta == null) {
            return null;
        }
        Object value = meta.get(key);
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    public boolean getMetaBoolean(String key) {
        if (meta == null) {
            return false;
        }
        Object value = meta.get(key);
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof String s) {
            return Boolean.parseBoolean(s);
        }
        return false;
    }
}