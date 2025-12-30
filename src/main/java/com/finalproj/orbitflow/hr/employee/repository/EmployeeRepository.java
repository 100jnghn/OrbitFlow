package com.finalproj.orbitflow.hr.employee.repository;

import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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


    List<Employee> findAllByStatus(EmployeeStatus status);

    int countByCompanyIdAndStatus(Long companyId, EmployeeStatus status);

    List<Employee> findByCompany_IdAndOrganization_IdAndPositionCategory_IdAndStatus(
            Long companyId,
            Long organizationId,
            Long positionCategoryId,
            EmployeeStatus status
    );


    @Query("""
        select e
        from Employee e
        join e.positionCategory pc
        join e.organization o
        where o.id = :organizationId
          and pc.orgCategory.id = :orgCategoryId
          and pc.isHead = true
          and pc.isActive = true
          and e.status = "ACTIVE"
    """)
    Optional<Employee> findHeadByOrganizationAndOrgCategory(
            @Param("organizationId") Long organizationId,
            @Param("orgCategoryId") Long orgCategoryId
    );

    @Query("""
        select case when count(e) > 0 then true else false end
        from Employee e
        join e.organization o
        join e.positionCategory pc
        where e.id = :employeeId
          and o.id = :organizationId
          and pc.id = :positionCategoryId
          and e.status = "ACTIVE"
          and pc.isActive = true
    """)
    boolean existsInOrgAndPositionCategory(
            @Param("employeeId") Long employeeId,
            @Param("organizationId") Long organizationId,
            @Param("positionCategoryId") Long positionCategoryId
    );


    @Query("""
        select e
        from Employee e
        join e.organization o
        join e.positionCategory pc
        where o.id = :orgId
          and pc.isHead = true
          and pc.isActive = true
          and pc.orgCategory.id = o.categoryId
          and e.status = com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus.ACTIVE
        order by e.id asc
    """)
    List<Employee> findHeadsByOrgId(@Param("orgId") Long orgId);

    default Optional<Employee> findHeadByOrgId(Long orgId) {
        List<Employee> heads = findHeadsByOrgId(orgId);
        return heads.isEmpty() ? Optional.empty() : Optional.of(heads.get(0));
    }

    @Query("""
    select e
    from Employee e
    join e.organization o
    join e.positionCategory pc
    where o.id = :orgId
      and pc.id = :positionCategoryId
      and e.status = :status
    """)
    Optional<Employee> findHeadByOrgIdAndPositionCategoryIdAndStatus(
            @Param("orgId") Long orgId,
            @Param("positionCategoryId") Long positionCategoryId,
            @Param("status") EmployeeStatus status
    );

    @Query("""
    select e
    from Employee e
    join e.organization o
    join e.positionCategory pc
    where e.id = :employeeId
      and o.id = :orgId
      and pc.id = :positionCategoryId
      and e.status = :status
    """)
    Optional<Employee> findByIdAndOrgIdAndPositionCategoryIdAndStatus(
            @Param("employeeId") Long employeeId,
            @Param("orgId") Long orgId,
            @Param("positionCategoryId") Long positionCategoryId,
            @Param("status") EmployeeStatus status
    );

    List<Employee> findByStatus(EmployeeStatus employeeStatus);

    @Query("""
    select e
    from Employee e
    join e.organization o
    where e.company.id = :companyId
      and (:status is null or e.status = :status)
      and (
        :keyword is null
        or e.name like concat('%', :keyword, '%')
        or e.email like concat('%', :keyword, '%')
      )
""")
    Page<Employee> searchAdmin(
            @Param("companyId") Long companyId,
            @Param("keyword") String keyword,
            @Param("status") EmployeeStatus status,
            Pageable pageable
    );

}
