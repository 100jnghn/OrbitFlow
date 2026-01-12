package com.finalproj.orbitflow.hr.logAudit.enums;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AuditEventType
 * @since : 2025-12-16 화요일
 */
public enum AuditEventType {

    CREATE("생성"),
    UPDATE("정보 수정"),

    STATUS_CHANGE("상태 변경"),

    ACTIVATE("활성화"),
    DEACTIVATE("비활성화"),

    MOVE("조직 이동"),
    ASSIGN("직급/직책 부여"),
    UNASSIGN("직급/직책 해제");

    private final String displayName;

    AuditEventType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
