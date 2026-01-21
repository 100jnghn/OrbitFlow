package com.finalproj.orbitflow.board.boardpost.entity;

import com.finalproj.orbitflow.board.boardcategory.entity.BoardCategory;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import jakarta.persistence.*;
import org.hibernate.annotations.Formula;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "board_post")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardPost extends BaseEntity {

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

    @Lob
    @Column(name = "board_content", nullable = false, columnDefinition = "LONGTEXT")
    private String boardContent; // 게시글 내용

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0; // 게시글 조회수

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "board_post_id")
    private List<File> files; // 첨부파일 (FK) (Nullable)

    @Column(name = "deleted_at")
    private Instant deletedAt; // 삭제 일시 (소프트 삭제)

    @Formula("(SELECT COUNT(*) FROM comment c WHERE c.board_post_id = id AND c.deleted_at IS NULL)")
    private int commentCount; // 댓글 수

    // 게시글 수정 메서드
    public void update(String boardTitle, String boardContent, List<File> newFiles) {
        this.boardTitle = boardTitle;
        this.boardContent = boardContent;

        // 파일 교체 (기존 컬렉션을 유지하면서 clear 후 addAll 사용)
        // orphanRemoval = true인 경우 새 리스트로 교체하면 Hibernate 오류 발생
        if (this.files == null) {
            this.files = new java.util.ArrayList<>();
        }
        this.files.clear();
        if (newFiles != null && !newFiles.isEmpty()) {
            this.files.addAll(newFiles);
        }
    }

    // 조회수 증가 메서드
    public void increaseViewCount() {
        this.viewCount += 1;
    }

    // 소프트 삭제 처리
    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    public static BoardPost create(
            BoardCategory category,
            Employee writer,
            String title,
            String content) {
        BoardPost boardPost = new BoardPost();
        boardPost.category = category;
        boardPost.writer = writer;
        boardPost.boardTitle = title;
        boardPost.boardContent = content;
        boardPost.viewCount = 0;
        return boardPost;
    }

}
