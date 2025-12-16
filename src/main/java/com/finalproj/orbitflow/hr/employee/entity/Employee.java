package com.finalproj.orbitflow.hr.employee.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.employee.enums.EmploymentType;
import com.finalproj.orbitflow.hr.employee.enums.Gender;
import com.finalproj.orbitflow.hr.organization.entity.Organization;
import com.finalproj.orbitflow.hr.position.entity.Position;
import com.finalproj.orbitflow.hr.rank.entity.Rank;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 사원 정보를 관리하는 엔티티.
 * - 시스템에 로그인 가능한 최소 단위 사용자
 * - 사번(employeeNo)과 이메일(email)을 고유 값으로 관리
 * - 공통 Auditing 필드(BaseEntity)를 상속받아 생성/수정 일시 및 생성자/수정자 정보를 자동 관리한다.
 *
 * @author : seunga03
 * @filename : Employee
 * @since : 2025-12-15 월요일
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA가 쓰기 위한 기본 생성자는 열어두되, 개발자가 마음대로 new 하지는 못하게 막는 장치
@Table(
        name = "employee",
        uniqueConstraints = { // 엔티티의 무결성을 DB 레벨에서도 보장하기 위함
                @UniqueConstraint(columnNames = {"company_id", "employee_no"}),
                @UniqueConstraint(columnNames = {"email"}),
                @UniqueConstraint(columnNames = {"company_id", "internalPhone"})
        }
)
public class Employee extends BaseEntity {

    // TODO: nullable 넣을지 고민 필요

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;   // 사원 ID (PK)

    /* ==============================
       소속 정보
       ============================== */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rank_id")
    private Rank rank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;


    /* ==============================
       사원 기본 정보
       ============================== */

    @Column(name = "employee_no", length = 20)
    private String employeeNo; // 사번 (예: 20250001)

    @Column(length = 20)
    private String internalPhone; // 사내 번호 --> 사원 퇴사 시 null

    @Column(length = 20)
    private String phone; // 연락처 --> 사원 퇴사 시 null

    @Column(nullable = false, length = 50)
    private String name;       // 이름

    @Column(nullable = false, length = 100)
    private String email; // 퇴사 시 resigned_{employeeId}@orbitflow.local 로 치환

    @Column(nullable = false)
    private String password;   //  --> 사원 퇴사 시 랜덤값으로 치환

    /* ==============================
       인적 정보
       ============================== */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;     // 성별 : MALE(남성), FEMALE(여성)

    @Column
    private LocalDate birthDate; // 생년월일

    /* ==============================
       근무 정보
       ============================== */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmploymentType employmentType; // 고용 형태 : REGULAR(정규), NON_REGULAR(비정규)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployeeStatus status; // 재직 상태 : TEMP(임시 계정), ACTIVE(재직), SUSPENDED(정지), RESIGNED(퇴사)

    /**
     * 대표 관리자 생성
     **/
    public static Employee createAdmin(
            Company company,
            String email,
            String encodedPassword
    ) {
        Employee employee = new Employee();
        employee.company = company;
        employee.email = email;
        employee.password = encodedPassword;
        employee.status = EmployeeStatus.ACTIVE;
        employee.employmentType = EmploymentType.REGULAR;
        return employee;
    }

}
