package com.finalproj.orbitflow.board.boardCategory.repository;

import com.finalproj.orbitflow.board.boardCategory.entity.BoardCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardCategoryRepository extends JpaRepository<BoardCategory, Long> {

    // =========================================================================
    // 1. 일반 카테고리 조회 및 관리
    // =========================================================================

    /**
     * [관리자용 - 수정 페이지 로드용] 활성화된 일반 카테고리 단건 조회(상세조회)
     *
     * @param id 게시판 카테고리 ID
     * @return 활성화된 게시판 카테고리 엔티티
     */
    Optional<BoardCategory> findByIdAndDeletedAtIsNull(Long id);

    /**
     * [관리자용] 활성화된 일반 카테고리 목록 조회 (페이징, 수정/삭제 대상)
     */
    Page<BoardCategory> findAllByCompany_Id(
            Long companyId,
            Pageable pageable
    );

    /**
     * [사용자용] 권한을 부여받은 일반 카테고리 목록 조회
     * 특정 직원이 접근 권한(BoardPermission)을 가진, 활성화된(isActivated=true), 소프트 삭제되지 않은 일반 게시판만 조회합니다.
     *
     * @param employeeId 직원 ID
     * @return 권한이 부여된 일반 카테고리 목록
     */
    @Query("""
                SELECT DISTINCT bc
                FROM BoardCategory bc
                JOIN bc.boardPermissions bp
                WHERE bp.employee.id = :employeeId
                  AND bc.isActivated = true
                  AND bc.deletedAt IS NULL
            """)
    List<BoardCategory> findAllActiveGeneralBoardsByPermission(@Param("employeeId") Long employeeId);


    // =========================================================================
    // 2. 조직 게시판 (Organization Board) 조회 및 관리
    // =========================================================================

    /**
     * [관리자용] 회사 전체 조직 게시판 목록 출력 (활성화/비활성화 모두 포함)
     * organization 필드가 null이 아닌 모든 게시판을 조직 게시판 관리 대상으로 가정합니다.
     *
     * @param companyId 회사 ID
     * @return 해당 회사에 속한 모든 조직 게시판 목록
     */
    @Query("SELECT bc FROM BoardCategory bc WHERE bc.company.id = :companyId AND bc.organization.id IS NOT NULL")
    List<BoardCategory> findAllOrganizationBoardsByCompanyId(@Param("companyId") Long companyId);

    /**
     * [사용자용] 본인 소속 조직 게시판 목록 조회 (활성화된 게시판만)
     * 사용자가 속한 조직 ID와 일치하는 활성화된 게시판만 조회합니다.
     *
     * @param orgId 사용자 조직 ID
     * @return 해당 조직에 할당된 활성화된 게시판 목록
     */
    @Query("SELECT bc FROM BoardCategory bc WHERE bc.organization.id = :orgId AND bc.isActivated = true")
    List<BoardCategory> findAllActiveOrganizationBoardsByOrgId(@Param("orgId") Long orgId);

}
