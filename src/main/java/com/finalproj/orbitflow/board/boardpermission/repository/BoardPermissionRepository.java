package com.finalproj.orbitflow.board.boardpermission.repository;

import com.finalproj.orbitflow.board.boardpermission.entity.BoardPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardPermissionRepository extends JpaRepository<BoardPermission, Long> {

    /**
     * 특정 직원이 권한을 부여받은 모든 게시판 권한 레코드를 조회합니다.
     * @param employeeId 직원 ID
     * @return 권한이 부여된 BoardPermission 목록
     */
    List<BoardPermission> findAllByEmployee_Id(Long employeeId);

    /**
     * 특정 게시판에 대해 권한을 가진 모든 BoardPermission 레코드를 조회합니다. (관리자 권한 부여/조회용)
     * @param boardCategoryId 게시판 카테고리 ID
     * @return 해당 게시판에 권한이 있는 BoardPermission 목록
     */
    List<BoardPermission> findAllByBoardCategory_Id(Long boardCategoryId);

    /**
     * 특정 직원과 특정 게시판 카테고리의 권한 존재 여부 확인 및 조회
     * @param employeeId 직원 ID
     * @param boardCategoryId 게시판 카테고리 ID
     * @return BoardPermission 엔티티
     */
    Optional<BoardPermission> findByEmployee_IdAndBoardCategory_Id(Long employeeId, Long boardCategoryId);



    boolean existsByBoardCategory_Id(Long boardCategoryId);
}