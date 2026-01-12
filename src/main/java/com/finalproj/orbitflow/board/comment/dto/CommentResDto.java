package com.finalproj.orbitflow.board.comment.dto;

import com.finalproj.orbitflow.board.comment.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

public class CommentResDto {

    // 댓글 목록 조회
    @Getter
    @Builder
    public static class ListInfo {
        private Long commentId;
        private Long writerId;
        private String writerName;
        private String content;
        private Instant createdAt;
        private Instant updatedAt;

        public static ListInfo from(Comment comment) {
            return ListInfo.builder()
                    .commentId(comment.getId())
                    .writerId(comment.getWriter().getId())
                    .writerName(comment.getWriter().getName())
                    .content(comment.getCommentContent())
                    .createdAt(comment.getCreatedAt())
                    .updatedAt(comment.getUpdatedAt())
                    .build();
        }
    }

    /** 댓글 상세 / 작성 / 수정 응답 */
    @Getter
    @Builder
    public static class DetailInfo {
        private Long commentId;
        private Long boardId;
        private Long writerId;
        private String writerName;
        private String content;
        private Instant createdAt;
        private Instant updatedAt;

        public static DetailInfo from(Comment comment) {
            return DetailInfo.builder()
                    .commentId(comment.getId())
                    .boardId(comment.getBoardPost().getId())
                    .writerId(comment.getWriter().getId())
                    .writerName(comment.getWriter().getName())
                    .content(comment.getCommentContent())
                    .createdAt(comment.getCreatedAt())
                    .updatedAt(comment.getUpdatedAt())
                    .build();
        }
    }
}
