package com.finalproj.orbitflow.approval.employeeSignature.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : EmployeeSignature
 * @since : 25. 12. 16. 화요일
 **/


@Entity
@Table(
        name = "employee_signature",
        indexes = {
                @Index(name = "idx_emp_sig_company_employee", columnList = "company_id, employee_id")
        }
)
@Getter
@NoArgsConstructor
public class EmployeeSignature extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_signature_id")
    private Long id;

    /* =========================
       Company (멀티 테넌트)
       ========================= */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /* =========================
       Employee (서명 소유자)
       ========================= */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /* =========================
       Signature Image File
       ========================= */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    /* =========================
       활성 서명 여부
       ========================= */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /* =========================
       생성 메서드
       ========================= */
    public static EmployeeSignature create(
            Company company,
            Employee employee,
            File file,
            boolean isActive
    ) {
        EmployeeSignature signature = new EmployeeSignature();
        signature.company = company;
        signature.employee = employee;
        signature.file = file;
        signature.isActive = isActive;
        return signature;
    }

    /* =========================
       서명 비활성화
       ========================= */
    public void deactivate() {
        this.isActive = false;
    }
}