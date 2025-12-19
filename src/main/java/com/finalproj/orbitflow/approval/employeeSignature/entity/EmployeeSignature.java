package com.finalproj.orbitflow.approval.employeeSignature.entity;

import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
                @Index(
                        name = "idx_emp_sig_company_employee",
                        columnList = "company_id, employee_id"
                )
        }
)
@Getter
@NoArgsConstructor
public class EmployeeSignature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public static EmployeeSignature create(
            Company company,
            Employee employee,
            File file
    ) {
        EmployeeSignature signature = new EmployeeSignature();
        signature.company = company;
        signature.employee = employee;
        signature.file = file;
        signature.isActive = true;
        return signature;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
