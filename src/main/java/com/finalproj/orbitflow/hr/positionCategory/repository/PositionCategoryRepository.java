package com.finalproj.orbitflow.hr.positionCategory.repository;

import com.finalproj.orbitflow.hr.positionCategory.dto.PositionCategoryListDto;
import com.finalproj.orbitflow.hr.positionCategory.entity.PositionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : PositionCategoryRepository
 * @since : 2025-12-16 화요일
 */
public interface PositionCategoryRepository extends JpaRepository<PositionCategory, Long> {

    List<PositionCategory> findByCompanyId(Long companyId);

    List<PositionCategory> findByCompanyIdAndIsActiveTrue(Long companyId);

    long countByCompanyIdAndIsActiveTrue(Long companyId);

    @Query("""
                select max(pc.orderIndex)
                from PositionCategory pc
                where pc.company.id = :companyId
                  and pc.isActive = true
            """)
    Integer findMaxActiveOrderIndexByCompanyId(@Param("companyId") Long companyId);

    List<PositionCategory> findByCompanyIdOrderByIsActiveDescOrderIndexAscCreatedAtDesc(Long companyId);

    boolean existsByCompanyIdAndOrgCategoryIdAndIsHeadTrue(Long companyId, Long id);

    @Query("""
    select p
    from PositionCategory p
    where p.orgCategory.id = :orgCategoryId
      and p.isHead = true
""")
    Optional<PositionCategory> findHeadPositionByOrgCategoryId(
            @Param("orgCategoryId") Long orgCategoryId
    );

    /* 목록 */
    List<PositionCategory> findByCompanyIdAndIsActiveTrueOrderByOrderIndexAsc(Long companyId);

    boolean existsByCompanyIdAndNameAndIdNot(
            Long companyId,
            String name,
            Long id
    );

    /* 직책 부여 인원수 */
    @Query("""
            select new com.finalproj.orbitflow.hr.positionCategory.dto.PositionCategoryListDto(
                pc.id, pc.name, oc.id, oc.name, parent.id, parent.name, 
                pc.isHead, pc.isActive, count(e.id), pc.orderIndex
                )
            from PositionCategory pc
            join pc.orgCategory oc
            left join pc.parent parent
            left join Employee e
                on e.positionCategory.id = pc.id
               and e.status = com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus.ACTIVE
            where pc.company.id = :companyId
              and (:includeInactive = true or pc.isActive = true)
            group by
                pc.id,
                pc.name,
                oc.id,
                oc.name,
                parent.id,
                parent.name,
                pc.isHead,
                pc.isActive,
                pc.orderIndex
            order by pc.orderIndex asc
                """)
    List<PositionCategoryListDto> findAllWithAssignedCount(
            @Param("companyId") Long companyId,
            @Param("includeInactive") boolean includeInactive
    );

    boolean existsByCompanyIdAndOrgCategoryIdAndName(
            Long companyId,
            Long orgCategoryId,
            String name
    );

}