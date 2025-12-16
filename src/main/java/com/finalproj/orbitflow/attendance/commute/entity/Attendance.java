package com.finalproj.orbitflow.attendance.commute.entity;

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
    @Column(name = "attendance_id", nullable = false)
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


    @Column(name = "status", length = 50)
    private String status; // 최종 근태 상태 (지각/결근/정상근무 등)

    @Column(name = "applied_rule_id")
    private Long appliedRuleId; // 최종 적용된 규칙 아이디 (FK: attendance_rule)

    @Column(name = "is_corrected", nullable = false)
    private Boolean isCorrected = false; // 정정 처리 여부

    @Column(name = "correction_reason", length = 255)
    private String correctionReason; // 정정 사유 (관리자 최종 사유)

    // FK 관계는 별도의 Service/Repository 계층에서 관리하거나,
    // @ManyToOne 매핑을 통해 Employee와 AttendanceRule 엔티티에 연결할 수 있습니다.
    // 예시에서는 단순화를 위해 ID 필드로만 남겨두었습니다.
}