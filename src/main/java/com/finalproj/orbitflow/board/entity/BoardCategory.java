package com.finalproj.orbitflow.board.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "board_category")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long Id; // 게시판 고유 ID (PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company; // 회사 ID (FK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_type", nullable = false)
    private Organization organization; // 조직 ID (FK)

    @Column(name = "board_name", length = 100, nullable = false)
    private String boardName; // 게시판 이름

    @Column(name = "board_type", length = 50, nullable = false)
    private String boardType; // 게시판 유형 ('공지사항', '자유게시판' 등)

    @Column(name = "is_activated", nullable = false)
    private boolean isActivated; // 사용 여부 (1: 활성화, 0: 비활성화)

    @Column(name = "comment_activated", nullable = false)
    private boolean commentActivated; // 댓글 기능 활성화 여부

    // 게시판 수정 메서드
    public void update(String boardName, String boardType, boolean isActivated, boolean commentActivated) {
        this.boardName = boardName;
        this.boardType = boardType;
        this.isActivated = isActivated;
        this.commentActivated = commentActivated;
    }

}

