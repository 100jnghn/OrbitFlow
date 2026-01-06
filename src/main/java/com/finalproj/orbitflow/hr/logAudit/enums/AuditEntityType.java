package com.finalproj.orbitflow.hr.logAudit.enums;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AuditEntityType
 * @since : 2025-12-16 화요일
 */
public enum AuditEntityType {

    COMPANY("회사"),
    ORGANIZATION("조직"),
    EMPLOYEE("사원"),
    HR_RANK("직급"),
    POSITION("직책"),
    ORG_POSITION_USAGE("조직-직책 정책");

    private final String displayName;

    AuditEntityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
