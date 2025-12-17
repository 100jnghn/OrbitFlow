package com.finalproj.orbitflow.board.boardCategory.dto;

import com.finalproj.orbitflow.board.boardCategory.entity.BoardCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

public class BoardCategoryResDto {

    /**
     * 게시판(카테고리) 목록 및 단건 조회 응답 DTO
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
        private boolean isActivated; // 사용 여부
        private boolean commentActivated; // 댓글 기능 활성화 여부
        private Instant createdAt;
        private Instant updatedAt;
        private Instant deletedAt; // 삭제 일시

        public static Category from(BoardCategory category) {
            return Category.builder()
                    .id(category.getId())
                    // Company ID 추출
                    .companyId(category.getCompany().getId())
                    // Organization ID 추출 (null 체크)
                    .organizationId(category.getOrganization() != null ? category.getOrganization().getId() : null)
                    .boardName(category.getBoardName())
                    .boardType(category.getBoardType())
                    .isActivated(category.isActivated())
                    .commentActivated(category.isCommentActivated())
                    // BaseEntity에서 LocalDateTime을 사용한다고 가정
                    .createdAt(category.getCreatedAt())
                    .updatedAt(category.getUpdatedAt())
                    .deletedAt(category.getDeletedAt())
                    .build();
        }
    }
}