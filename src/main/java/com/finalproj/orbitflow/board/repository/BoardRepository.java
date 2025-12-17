package com.finalproj.orbitflow.board.repository;

import com.finalproj.orbitflow.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {



    Optional<Board> findByIdAndDeletedAtIsNull(Long id); // 소프트 삭제되지 않은 게시글 단건 조회


    Page<Board> findAllByDeletedAtIsNull(Pageable pageable); // 소프트 삭제되지 않은 게시글 목록 조회 (페이징)

}

