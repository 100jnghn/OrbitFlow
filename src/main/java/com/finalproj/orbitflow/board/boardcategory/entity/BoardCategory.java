package com.finalproj.orbitflow.board.boardcategory.entity;

import com.finalproj.orbitflow.board.boardpermission.entity.BoardPermission;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "board_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 게시판 고유 ID (PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company; // 회사 ID (FK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = true)
    private Organization organization; // 조직 ID (FK)

    @Column(name = "board_name", length = 100, nullable = false)
    private String boardName; // 게시판 이름

    @Column(name = "board_type", length = 50, nullable = false)
    private String boardType; // 게시판 유형 ('공지사항', '자유게시판' 등)

    @Column(name = "is_activated", nullable = false)
    private boolean isActivated; // 사용 여부 (1: 활성화, 0: 비활성화)

    @Column(name = "comment_activated", nullable = false)
    private boolean commentActivated; // 댓글 기능 활성화 여부

    @Column(name = "deleted_at")
    private Instant deletedAt; // 삭제 일시 (소프트 삭제)

    // *** JPQL 최적화를 위해 BoardPermission 엔티티와의 1:N 관계 추가 ***
    @OneToMany(mappedBy = "boardCategory")
    private List<BoardPermission> boardPermissions;

    // 게시판 수정 메서드
    public void update(String boardName, String boardType, boolean isActivated, boolean commentActivated) {
        this.boardName = boardName;
        this.boardType = boardType;
        this.isActivated = isActivated;
        this.commentActivated = commentActivated;
    }

    // 게시판 소프트 삭제 처리
    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    // 게시판 활성화 여부 확인 (소프트 삭제 및 isActivated 상태 모두 고려)
    public boolean isActive() {
        return this.isActivated && this.deletedAt == null;
    }

}

