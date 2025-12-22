package com.finalproj.orbitflow.hr.employee.repository;

import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    boolean existsByCompanyIdAndOrganizationIdAndStatusNot(
            Long companyId,
            Long organizationId,
            EmployeeStatus status
    );
  
    /**
     * 회사 ID + 이름 또는 사번으로 사원 검색
     */
    @Query("SELECT e FROM Employee e WHERE e.company.id = :companyId " +
           "AND (e.name LIKE CONCAT('%', :keyword, '%') OR e.employeeNo LIKE CONCAT('%', :keyword, '%')) " +
           "AND e.status = com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus.ACTIVE")
    List<Employee> searchByCompanyIdAndKeyword(
            @Param("companyId") Long companyId,
            @Param("keyword") String keyword

    );

    boolean existsByEmail(String email);

    /**
     * 직급에 부여된 사원 수 조회
     */
    long countByCompanyIdAndRank_Id(Long companyId, Long rankId);

    List<Employee> findAllByStatus(String active);
}
