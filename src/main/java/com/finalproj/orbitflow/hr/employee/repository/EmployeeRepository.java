package com.finalproj.orbitflow.hr.employee.repository;

import com.finalproj.orbitflow.hr.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 사원(Employee) 엔티티에 대한 데이터 접근을 담당하는 Repository.
 * - 이메일 기반 로그인 조회
 * - 사번 기반 사원 조회
 * Spring Data JPA를 사용하여 기본 CRUD와 도메인 기반 조회 메서드를 제공한다.
 *
 * @author : seunga03
 * @filename : EmployeeRepository
 * @since : 2025-12-15 월요일
 */

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * 이메일(로그인 ID)을 기준으로 사원 조회
     */
    Optional<Employee> findByEmail(String email);

    /**
     * 회사 ID + 사번을 기준으로 사원 조회
     */
    Optional<Employee> findByCompanyIdAndEmployeeNo(Long companyId, String employeeNo);

    /**
     * 회사 ID + 이메일(로그인 ID)을 기준으로 사원 조회
     */
    Optional<Employee> findByCompanyIdAndEmail(Long companyId, String email);


    List<Employee> findAllByIdIn(List<Long> ids);

}
