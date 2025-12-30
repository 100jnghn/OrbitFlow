package com.finalproj.orbitflow.board.boardPermission.dto;

import com.finalproj.orbitflow.board.boardPermission.entity.BoardPermission;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

public class BoardPermissionResDto {

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
            private String email; // 이메일 (Employee.email)
            private String departmentName; // 부서 이름 (Employee.organization.name)
            private String rankName; // 직급 이름 (Employee.rank.name)

            public static EmployeeInfo from(Employee employee) {
                return EmployeeInfo.builder()
                        .employeeId(employee.getId())
                        .employeeNumber(employee.getEmployeeNo())
                        .name(employee.getName())
                        .email(employee.getEmail() != null ? employee.getEmail() : "")
                        // Organization과 Rank 엔티티에서 이름을 가져온다고 가정
                        .departmentName(employee.getOrganization() != null ? employee.getOrganization().getName() : "부서 정보 없음")
                        .rankName(employee.getRank() != null ? employee.getRank().getName() : "직급 정보 없음")
                        .build();
            }
        }
    }
}