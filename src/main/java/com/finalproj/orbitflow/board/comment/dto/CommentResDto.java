package com.finalproj.orbitflow.board.comment.dto;

import com.finalproj.orbitflow.board.comment.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

public class CommentResDto {

    @Getter
    @Builder
    public static class ListInfo {
        private Long commentId;
        private Long writerId;
        private String writerName;
        private String content;
        private Instant createdAt;

        public static ListInfo from(Comment c) {
            return ListInfo.builder()
                    .commentId(c.getId())
                    .writerId(c.getWriter().getId())
                    .writerName(c.getWriter().getName())
                    .content(c.getCommentContent())
                    .createdAt(c.getCreatedAt())
                    .build();
        }
    }
}
