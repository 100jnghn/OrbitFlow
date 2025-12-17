package com.finalproj.orbitflow.board.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "board")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 게시글 ID (PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_category_id", nullable = false)
    private BoardCategory category; // 게시판 카테고리 (FK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee writer; // 작성자 사원 (FK)

    @Column(name = "board_title", length = 255, nullable = false)
    private String boardTitle; // 게시글 제목

    @Lob // TEXT 길이 제한 없음
    @Column(name = "board_content", nullable = false)
    private String boardContent; // 게시글 내용

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0; // 게시글 조회수

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file; // 첨부파일 (FK) (Nullable)

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // 삭제 일시 (소프트 삭제)

    // 게시글 수정 메서드
    public void update(String boardTitle, String boardContent, File file) {
        this.boardTitle = boardTitle;
        this.boardContent = boardContent;
        this.file = file;
    }

    // 조회수 증가 메서드
    public void incrementViewCount() {
        this.viewCount += 1;
    }

    // 소프트 삭제 처리
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

}
