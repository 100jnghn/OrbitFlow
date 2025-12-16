package com.finalproj.orbitflow.resource.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ResourceStatusCode
 * @since : 2025-12-16 오전 10:41 화요일
 */
@Getter
@RequiredArgsConstructor
public enum ResourceStatusCode {
    AVAILABLE("사용 가능"),
    INSPECTION("점검 중"),
    UNAVAILABLE("사용 불가"),
    DELETED("삭제됨"),
    ETC("기타");

    private final String description;
}