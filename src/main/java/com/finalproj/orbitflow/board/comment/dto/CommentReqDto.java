package com.finalproj.orbitflow.board.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

public class CommentReqDto {

    @Getter
    @Setter
    public static class Create {
        @NotBlank(message = "댓글 내용은 필수입니다.")
        @Size(max = 500, message = "댓글은 500자 이하여야 합니다.")
        private String commentContent;
    }

    @Getter
    @Setter
    public static class Update {
        @NotBlank(message = "댓글 내용은 필수입니다.")
        @Size(max = 500, message = "댓글은 500자 이하여야 합니다.")
        private String commentContent;
    }
}

