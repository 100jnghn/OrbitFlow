package com.finalproj.orbitflow.approval.document.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FieldType
 * @since : 25. 12. 23. 화요일
 **/


@Getter
@RequiredArgsConstructor
public enum FieldType {

    DOCUMENT_TITLE("document-title", true),

    TEXT("text", true),
    TEXTAREA("textarea", true),
    NUMBER("number", true),

    TIME("time", true),
    TIME_RANGE("time-range", true),

    DATE("date", true),
    DATE_RANGE("date-range", true),

    RADIO("radio", true),
    CHECKBOX("checkbox", true),

    LEAVE_DATE_RANGE("leave-date-range", true),

    TABLE("table", true),

    IMAGE("image", true),

    CURRENCY("currency", true),
    ADDRESS("address", true),

    EMPLOYEE_SEARCH("employee-search", true),
    DEPARTMENT_SEARCH("department-search", true),

    DIVIDER("divider", false),
    NOTICE("notice", false);

    private final String code;
    private final boolean editable;

    public static FieldType from(String code) {
        return Arrays.stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unknown fieldType: " + code)
                );
    }
}
