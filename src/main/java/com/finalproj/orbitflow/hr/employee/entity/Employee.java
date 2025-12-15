package com.finalproj.orbitflow.hr.employee.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : Employee
 * @since : 2025-12-15 월요일
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Employee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;   // PK

    @Column(nullable = false, unique = true, length = 20)
    private String employeeNo; // 사번 (예: 20250001)

    @Column(nullable = false, unique = true, length = 100)
    private String email;      // 로그인 ID

    @Column(nullable = false)
    private String password;
}
