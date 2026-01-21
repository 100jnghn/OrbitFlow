package com.finalproj.orbitflow.board.boardcategory.repository;

import com.finalproj.orbitflow.board.boardcategory.entity.BoardCategory;
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
        // 1. 일반 카테고리 조회
        // =========================================================================

        /** [관리자용] 단건 조회(수정 페이지 로드용, 삭제되지 않은 게시판만) */
        Optional<BoardCategory> findByIdAndDeletedAtIsNull(Long id);

        /** [관리자용] 활성화/비활성화 포함 일반 카테고리 목록 조회(페이징) */
        Page<BoardCategory> findAllByCompany_IdAndOrganizationIsNullAndDeletedAtIsNull(
                        Long companyId,
                        Pageable pageable);

        /**
         * [사용자용][A안]
         * - 권한(permissions)이 하나도 없는 일반 게시판 = 전 직원에게 노출(공용)
         * - 권한(permissions)이 존재하는 일반 게시판 = 권한 있는 직원에게만 노출(제한)
         */
        @Query("""
                            SELECT DISTINCT bc
                            FROM BoardCategory bc
                            LEFT JOIN bc.boardPermissions bp
                                ON bp.employee.id = :employeeId
                            WHERE bc.company.id = :companyId
                              AND bc.isActivated = true
                              AND bc.deletedAt IS NULL
                              AND bc.organization IS NULL
                              AND (
                                    bp.id IS NOT NULL
                                    OR NOT EXISTS (
                                        SELECT 1
                                        FROM BoardPermission bp2
                                        WHERE bp2.boardCategory = bc
                                    )
                                  )
                        """)
        List<BoardCategory> findAccessiblePublicBoards(
                        @Param("companyId") Long companyId,
                        @Param("employeeId") Long employeeId);

        // =========================================================================
        // 2. 조직 게시판 조회
        // =========================================================================

        /** [관리자용] 회사 전체 조직 게시판 목록 조회 (페이징) */
        Page<BoardCategory> findByCompany_IdAndOrganizationIsNotNullAndDeletedAtIsNull(
                        Long companyId,
                        Pageable pageable);

        /** [사용자용] 본인 소속 조직 게시판 조회 (활성화된 게시판만) */
        List<BoardCategory> findByOrganization_IdAndIsActivatedTrueAndDeletedAtIsNull(Long orgId);

        /** [사용자용] 소속 조직 및 상위 조직 게시판 조회 (활성화된 게시판만) */
        List<BoardCategory> findByOrganization_IdInAndIsActivatedTrueAndDeletedAtIsNull(List<Long> orgIds);

        /** [ADMIN용] 활성화된 모든 공용 게시판 조회 */
        List<BoardCategory> findByCompany_IdAndOrganizationIsNullAndIsActivatedTrueAndDeletedAtIsNull(Long companyId);

        /** [관리자용] 조직게시판 자동생성 */
        boolean existsByCompany_IdAndOrganization_IdAndDeletedAtIsNull(Long companyId, Long organizationId);

        Optional<BoardCategory> findByOrganization_IdAndDeletedAtIsNull(Long organizationId);

        // 특정 타입의 게시판 존재 여부 확인 (삭제되지 않은 것 중)
        boolean existsByCompany_IdAndBoardTypeAndDeletedAtIsNull(Long companyId, String boardType);

}
