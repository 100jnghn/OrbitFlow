package com.finalproj.orbitflow.board.boardpost.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class BoardPostReqDto {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Create {

        @NotNull(message = "게시판 카테고리는 필수입니다.")
        private Long categoryId;

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        private String boardTitle;

        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 10000, message = "내용은 10000자 이하여야 합니다.")
        private String boardContent;

        // 첨부파일 ID (없으면 null)
        private Long fileId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Update {
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        private String boardTitle;

        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 10000, message = "내용은 10000자 이하여야 합니다.")
        private String boardContent;

        // 유지할 기존 파일 ID 목록
        private java.util.List<Long> keptFileIds;
    }
}
