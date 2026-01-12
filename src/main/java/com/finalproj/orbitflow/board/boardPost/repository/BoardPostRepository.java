package com.finalproj.orbitflow.board.boardPost.repository;

import com.finalproj.orbitflow.board.boardPost.entity.BoardPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface BoardPostRepository extends JpaRepository<BoardPost, Long>, JpaSpecificationExecutor<BoardPost> {

    // 소프트 삭제되지 않은 게시글 단건 조회
    Optional<BoardPost> findByIdAndDeletedAtIsNull(Long id);

    // 소프트 삭제되지 않은 게시글 목록 조회 (페이징)
    Page<BoardPost> findAllByDeletedAtIsNull(Pageable pageable);

    // // 카테고리별 게시글 조회 (소프트 삭제 제외)
    // Page<BoardPost> findAllByCategory_IdAndDeletedAtIsNull(Long categoryId,
    // Pageable pageable);

    boolean existsByCategory_Id(Long boardCategoryId);
}
