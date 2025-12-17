package com.finalproj.orbitflow.board.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class AdminBoardReqDto {
    /**
     * 관리자가 게시판(카테고리) 생성 및 수정 시 사용하는 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Category {

        // 게시판이 속할 회사 ID (필수)
        @NotNull(message = "회사 ID는 필수입니다.")
        private Long companyId;

        // 조직 ID (Organization ID) - 일반 게시판일 경우 null 허용
        private Long organizationId;

        // 게시판 이름
        @NotBlank(message = "게시판 이름은 필수입니다.")
        private String boardName;

        // 게시판 유형 ('공지사항', '자유게시판' 등)
        @NotBlank(message = "게시판 유형은 필수입니다.")
        private String boardType;

        // 사용 여부 (true: 활성화, false: 비활성화)
        @NotNull(message = "활성화 여부는 필수입니다.")
        private Boolean isActivated;

        // 댓글 기능 활성화 여부
        @NotNull(message = "댓글 활성화 여부는 필수입니다.")
        private Boolean commentActivated;
    }


    /**
     * 관리자가 특정 게시판에 권한을 부여/해제할 직원 목록을 요청하는 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Permission {

        // 권한을 설정할 게시판 카테고리 ID
        @NotNull(message = "게시판 카테고리 ID는 필수입니다.")
        private Long boardCategoryId;

        // 권한을 부여/제거할 직원 ID 목록
        @NotEmpty(message = "직원 ID는 최소 1명 이상이어야 합니다.")
        private List<Long> employeeIds;
    }
}