package com.finalproj.orbitflow.attendance.correctionHistory.entity;


import com.finalproj.orbitflow.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "correction_history")
public class CorrectionHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 출퇴근 기록 ID (Foreign Key)
     */
    @Column(name = "attendance_id", nullable = false)
    private Long attendanceId;

    /**
     * 원본 출근 시각
     */
    @Column(name = "original_commute_at")
    private LocalDateTime originalCommuteAt;

    /**
     * 원본 퇴근 시각
     */
    @Column(name = "original_leave_at")
    private LocalDateTime originalLeaveAt;

    /**
     * 수정된 출근 시각
     */
    @Column(name = "corrected_commute_at")
    private LocalDateTime correctedCommuteAt;

    /**
     * 수정된 퇴근 시각
     */
    @Column(name = "corrected_leave_at")
    private LocalDateTime correctedLeaveAt;

    /**
     * 수정 요청 사유
     */
    @Column(name = "correction_reason", length = 255)
    private String correctionReason;

    /**
     * 수정 요청 상태 (예: 'PENDING')
     */
    @Column(name = "correction_status", length = 50, nullable = false)
    private String correctionStatus;

    /**
     * 처리자 ID (Foreign Key)
     */
    @Column(name = "processed_by")
    private Long processedBy;

    /**
     * 처리 시각
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * 반려 사유
     */
    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    // 필요하다면, attendanceId나 processedBy에 대한
    // 연관 관계 매핑(ex. @ManyToOne)을 추가할 수 있습니다.
    // (참고: history_id 필드는 4번 테이블 정의서에 없지만, 이전 테이블과 연관이 있다면 포함)
}