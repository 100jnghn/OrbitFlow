package com.finalproj.orbitflow.board.dto.admin;

import com.finalproj.orbitflow.board.entity.BoardCategory;
import com.finalproj.orbitflow.board.entity.BoardPermission;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class AdminBoardResDto {

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

    /**
     * 특정 게시판에 권한이 부여된 직원 목록 응답 DTO
     */
    @Getter
    @Builder
    public static class Permission {

        private Long permissionId; // 권한 레코드 ID
        private Long boardCategoryId; // 게시판 ID

        private EmployeeInfo employeeInfo; // 권한을 부여받은 직원 정보

        public static Permission from(BoardPermission permission) {
            return Permission.builder()
                    .permissionId(permission.getId())
                    .boardCategoryId(permission.getBoardCategory().getId())
                    .employeeInfo(EmployeeInfo.from(permission.getEmployee()))
                    .build();
        }

        /**
         * BoardPermission 리스트를 응답 DTO 리스트로 변환하는 유틸리티 메서드
         */
        public static List<Permission> fromList(List<BoardPermission> permissions) {
            return permissions.stream()
                    .map(Permission::from)
                    .collect(Collectors.toList());
        }

        /**
         * 권한을 부여받은 직원의 핵심 정보 (Employee 엔티티 기반)
         */
        @Getter
        @Builder
        public static class EmployeeInfo {
            private Long employeeId;
            private String employeeNumber; // 사번 (Employee.employeeNo)
            private String name; // 이름 (Employee.name)
            private String departmentName; // 부서 이름 (Employee.organization.name)
            private String rankName; // 직급 이름 (Employee.rank.name)

            public static EmployeeInfo from(Employee employee) {
                return EmployeeInfo.builder()
                        .employeeId(employee.getId())
                        .employeeNumber(employee.getEmployeeNo())
                        .name(employee.getName())
                        // Organization과 Rank 엔티티에서 이름을 가져온다고 가정
                        .departmentName(employee.getOrganization() != null ? employee.getOrganization().getName() : "부서 정보 없음")
                        .rankName(employee.getRank() != null ? employee.getRank().getName() : "직급 정보 없음")
                        .build();
            }
        }
    }
}