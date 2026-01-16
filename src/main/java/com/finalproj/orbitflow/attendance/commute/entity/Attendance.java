package com.finalproj.orbitflow.attendance.commute.entity;

import aQute.bnd.annotation.headers.BundleLicense;
import com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus;
import com.finalproj.orbitflow.global.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_id", "work_date"}) // UK: uk_att_date
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id; // 아이디

    @Column(name = "company_id", nullable = false)
    private Long companyId; // 회사 아이디 (FK: company)

    @Column(name = "employee_id", nullable = false)
    private Long employeeId; // 기록한 사원 아이디 (FK: employee)

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate; // 근무 일자 (DATE)

    @Column(name = "commute_at")
    private LocalDateTime commuteAt; // 실제 출근 시간 (DATETIME)

    @Column(name = "leave_at")
    private LocalDateTime leaveAt; // 실제 퇴근 시간 (DATETIME)

    /**
     * 최종 근태 상태 (지각/결근/정상근무 등)
     * 중복된 String status 필드를 제거하고 Enum 타입으로 통일
     */
    @Enumerated(EnumType.STRING) // Enum 이름을 DB에 VARCHAR(50) 등으로 저장
    @Column(name = "status", length = 50, nullable = false) // length를 지정하여 DB 컬럼에 맞춤
    private AttendanceStatus status;

    @Column(name = "applied_rule_id")
    private Long appliedRuleId; // 최종 적용된 규칙 아이디 (FK: attendance_rule)

    @Column(name = "is_corrected", nullable = false)
    private Boolean isCorrected = false; // 정정 처리 여부

    @Column(name = "correction_reason", length = 255)
    private String correctionReason; // 정정 사유 (관리자 최종 사유)

    public void updateStatus(AttendanceStatus status, String correctionReason) {
        this.status = status;
        this.correctionReason = correctionReason;
        this.markAsCorrected(); // 상태 변경 시 자동으로 정정 여부 체크
    }

    public void markAsCorrected() {
        this.isCorrected = true;
    }


    public void updateTimeByAdmin(LocalDateTime commuteAt, LocalDateTime leaveAt) {

        this.isCorrected = true;

        if (commuteAt != null) {
            this.commuteAt = commuteAt;
        }
        if (leaveAt != null) {
            this.leaveAt = leaveAt;
        }
    }

    public void recordLeave() {
        if (this.leaveAt != null) {
            throw new BusinessException("이미 퇴근 처리되었습니다."); // 엔티티 내부에서 상태 검증
        }
        this.leaveAt = LocalDateTime.now(); // 내부 필드 직접 업데이트
    }

    public void updateStatusAutomatically(AttendanceStatus status) {
        this.status = status;
    }

    public void updateCommuteTime(LocalDateTime commuteAt){
        this.commuteAt = commuteAt;
    }




}