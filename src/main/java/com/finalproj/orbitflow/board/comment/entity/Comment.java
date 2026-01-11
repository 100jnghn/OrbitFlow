package com.finalproj.orbitflow.board.comment.entity;

import com.finalproj.orbitflow.board.boardPost.entity.Board;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 댓글 ID (PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board; // 게시글 (FK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee writer; // 작성자 사원 (FK)

    @Column(name = "comment_content", length = 500, nullable = false)
    private String commentContent; // 댓글 내용


    @Column(name = "deleted_at")
    private Instant deletedAt; // 삭제 일시 (소프트 삭제)

    // 댓글 수정 메서드
    public void updateContent(String commentContent) {
        this.commentContent = commentContent;
    }

    // 댓글 소프트 삭제 처리
    public void softDelete() {
        this.deletedAt = Instant.now();
    }

}