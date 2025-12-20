package com.finalproj.orbitflow.board.boardPost.repository;

import com.finalproj.orbitflow.board.boardPost.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long>, JpaSpecificationExecutor<Board> {


    // 소프트 삭제되지 않은 게시글 단건 조회
    Optional<Board> findByIdAndDeletedAtIsNull(Long id);

    // 소프트 삭제되지 않은 게시글 목록 조회 (페이징)
    Page<Board> findAllByDeletedAtIsNull(Pageable pageable);

//    // 카테고리별 게시글 조회 (소프트 삭제 제외)
//    Page<Board> findAllByCategory_IdAndDeletedAtIsNull(Long categoryId, Pageable pageable);

}

