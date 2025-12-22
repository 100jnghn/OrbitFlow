package com.finalproj.orbitflow.board.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

public class CommentReqDto {

    @Getter
    @Setter
    public static class Create {
        @NotBlank
        private String commentContent;
    }

    @Getter
    @Setter
    public static class Update {
        @NotBlank
        private String commentContent;
    }
}

