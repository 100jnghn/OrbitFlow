package com.finalproj.orbitflow.approval.attendance.event.entity;

import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.form.template.group.enums.BaseRole;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 외근/출장 상태 변경에 필요한 로그 엔티티
 *
 * @author : Choi MinHyeok
 * @filename : AttendanceEvent
 * @since : 26. 1. 1. 목요일
 **/

@Entity
@Table(name = "attendance_event")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(name = "base_role", nullable = false, length = 20)
    private BaseRole baseRole;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_document_id")
    private Document sourceDocument;


    public void updateEndDate(LocalDate actualEndDate) {
        this.actualEndDate = actualEndDate;
    }

}