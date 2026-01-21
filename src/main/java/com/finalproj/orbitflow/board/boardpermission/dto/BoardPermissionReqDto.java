package com.finalproj.orbitflow.board.boardpermission.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class BoardPermissionReqDto {

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