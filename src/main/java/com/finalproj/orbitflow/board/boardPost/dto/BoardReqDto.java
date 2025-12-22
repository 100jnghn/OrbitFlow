package com.finalproj.orbitflow.board.boardPost.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class BoardReqDto {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Create {

        @NotNull(message = "게시판 카테고리는 필수입니다.")
        private Long categoryId;

        @NotBlank(message = "제목은 필수입니다.")
        private String boardTitle;

        @NotBlank(message = "내용은 필수입니다.")
        private String boardContent;

        // 첨부파일 ID (없으면 null)
        private Long fileId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Update {
        @NotBlank
        private String boardTitle;

        @NotBlank
        private String boardContent;
    }
}
