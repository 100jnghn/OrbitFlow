package com.finalproj.orbitflow.attendance.commute.entity;


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
    @Column(name = "correction_id", nullable = false)
    private Long correctionId;

    @Column(name = "history_id", nullable = false)
    private Long historyId;         // 히스토리 ID (이전 테이블 정의서에 있었으나 4번 테이블에는 없음. 일단 포함)

    @Column(name = "attendance_id", nullable = false)
    private Long attendanceId;      // 근태 기록 ID (FK - attendance_record 테이블을 참조해야 함)

    @Column(name = "original_come_at")
    private LocalDateTime originalComeAt; // 정정 전 출근 시각

    @Column(name = "original_leave_at")
    private LocalDateTime originalLeaveAt; // 정정 전 퇴사 시간

    @Column(name = "correction_date", nullable = false)
    private LocalDateTime correctionDate; // 정정 처리 일시

    @Column(name = "correction_reason", length = 255)
    private String correctionReason; // 정정 사유

    @Column(name = "source_ip", length = 50)
    private String sourceIp;        // 수정 요청 IP

    // (참고: history_id 필드는 4번 테이블 정의서에 없지만, 이전 테이블과 연관이 있다면 포함)
}