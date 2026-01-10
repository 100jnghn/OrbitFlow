package com.finalproj.orbitflow.hr.logAudit.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AuditValueNormalizer
 * @since : 2026-01-10 토요일
 */
public final class AuditValueNormalizer {

    private AuditValueNormalizer() {}

    public static Object normalize(Object value) {
        if (value == null) return null;

        // 날짜 타입
        if (value instanceof LocalDate d) {
            return d.toString(); // yyyy-MM-dd
        }
        if (value instanceof LocalDateTime dt) {
            return dt.toString();
        }

        // Enum
        if (value instanceof Enum<?> e) {
            return e.name();
        }

        // 그 외는 그대로
        return value;
    }

    public static Map<String, Object> normalizeMap(Map<String, Object> source) {
        if (source == null) return null;

        Map<String, Object> result = new LinkedHashMap<>();
        for (var entry : source.entrySet()) {
            result.put(entry.getKey(), normalize(entry.getValue()));
        }
        return result;
    }
}
