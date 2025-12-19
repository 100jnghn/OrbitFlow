package com.finalproj.orbitflow.attendance.commute.entity;

import com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_id", "work_date"}) // UK: uk_att_date
})
@Getter
@Setter
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
}