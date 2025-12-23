package com.finalproj.orbitflow.hr.orgPositionUsage.repository;

import com.finalproj.orbitflow.hr.orgPositionUsage.entity.OrgPositionUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgPositionUsageRepository
 * @since : 2025-12-16 화요일
 */
public interface OrgPositionUsageRepository extends JpaRepository<OrgPositionUsage, Long> {

    /* 조직에서 허용된 직책 목록 */
    List<OrgPositionUsage> findByOrganization_Id(Long orgId);

    /* 정책 존재 여부 (사원 생성/수정 검증용) */
    boolean existsByOrganization_IdAndPositionCategory_Id(Long organizationId, Long positionCategoryId);

    /* 직책 비활성화 검증용 */
    boolean existsByCompany_IdAndPositionCategory_Id(Long companyId, Long positionCategoryId);

    /* 조직 정책 전체 삭제 (덮어쓰기 방식) */
    void deleteByCompany_IdAndOrganization_Id(Long companyId, Long organizationId);

    @Query("""
    select opp
    from OrgPositionUsage opp
    join fetch opp.positionCategory pc
    where opp.organization.id = :orgId
""")
    List<OrgPositionUsage> findAllUsagesByOrgId(Long orgId);

}