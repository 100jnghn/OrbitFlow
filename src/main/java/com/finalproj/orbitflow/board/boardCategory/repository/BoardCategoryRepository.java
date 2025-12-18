package com.finalproj.orbitflow.board.boardCategory.repository;

import com.finalproj.orbitflow.board.boardCategory.entity.BoardCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
            Pageable pageable
    );

    /** [사용자용] 권한이 부여된 활성 일반 게시판 목록 조회 */
    List<BoardCategory> findByBoardPermissions_Employee_IdAndIsActivatedTrueAndDeletedAtIsNull(Long employeeId);


    // =========================================================================
    // 2. 조직 게시판 조회
    // =========================================================================


    /** [관리자용] 회사 전체 조직 게시판 목록 조회 (페이징) */
    Page<BoardCategory> findByCompany_IdAndOrganizationIsNotNullAndDeletedAtIsNull(
            Long companyId,
            Pageable pageable
    );

    /** [사용자용] 본인 소속 조직 게시판 조회 (활성화된 게시판만) */
    List<BoardCategory> findByOrganization_IdAndIsActivatedTrueAndDeletedAtIsNull(Long orgId);

}
