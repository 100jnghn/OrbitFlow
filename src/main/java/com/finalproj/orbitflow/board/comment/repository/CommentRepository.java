package com.finalproj.orbitflow.board.comment.repository;

import com.finalproj.orbitflow.board.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByBoardIdAndDeletedAtIsNull(Long boardId, Pageable pageable);

}
