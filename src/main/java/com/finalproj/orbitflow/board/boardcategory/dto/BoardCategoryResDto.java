package com.finalproj.orbitflow.board.boardcategory.dto;

import com.finalproj.orbitflow.board.boardcategory.entity.BoardCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

public class BoardCategoryResDto {

    /**
     * 게시판 목록 조회 DTO (일반 / 조직 공용)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Category {

        private Long id; // 게시판 고유 ID
        private Long companyId; // 회사 ID
        private Long organizationId; // 조직 ID (null이면 일반 게시판)
        private String boardName; // 게시판 이름
        private String boardType; // 게시판 유형
        private boolean isActivated; // 활성화 여부
        private Instant createdAt;


        private Instant updatedAt;
        private Instant deletedAt; // 삭제 일시

        public static Category from(BoardCategory category) {
            return Category.builder()
                    .id(category.getId())
                    .companyId(category.getCompany().getId())
                    .organizationId(
                            category.getOrganization() != null
                                    ? category.getOrganization().getId()
                                    : null
                    )
                    .boardName(category.getBoardName())
                    .boardType(category.getBoardType())
                    .isActivated(category.isActivated())
                    .createdAt(category.getCreatedAt())
                    .build();
        }
    }

    /**
     * 게시판 상세 조회 DTO (관리자 - 수정 페이지)
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Detail {

        private Long id;
        private Long companyId;
        private String boardName;
        private String boardType;
        private boolean isActivated;
        private boolean commentActivated;

        public static Detail from(BoardCategory category) {
            return Detail.builder()
                    .id(category.getId())
                    .companyId(category.getCompany().getId())
                    .boardName(category.getBoardName())
                    .boardType(category.getBoardType())
                    .isActivated(category.isActivated())
                    .commentActivated(category.isCommentActivated())
                    .build();
        }
    }
}