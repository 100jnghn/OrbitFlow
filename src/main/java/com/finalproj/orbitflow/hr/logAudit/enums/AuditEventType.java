package com.finalproj.orbitflow.hr.logAudit.enums;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AuditEventType
 * @since : 2025-12-16 화요일
 */
public enum AuditEventType {
    CREATE,             // 신규 생성
    UPDATE,             // 일반 정보 수정

    STATUS_CHANGE,      // 사용자 상태 변경 (임시/재직/휴직/퇴사)

    ACTIVATE,           // 엔티티 활성화
    DEACTIVATE,         // 엔티티 비활성화

    MOVE,               // 조직 이동
    ASSIGN,             // 직급/직책 부여
    UNASSIGN            // 직급/직책 해제
}
