package com.finalproj.orbitflow.board.entity;

import com.finalproj.orbitflow.hr.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long Id; // 댓글 ID (PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board; // 게시글 (FK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee writer; // 작성자 사원 (FK)

    @Lob // TEXT 타입 매핑
    @Column(name = "comment_content", nullable = false)
    private String commentContent; // 댓글 내용

    // 댓글 수정 메서드
    public void updateContent(String commentContent) {
        this.commentContent = commentContent;

    }
}