package com.finalproj.orbitflow.board.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "board_permission",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"employee_id", "board_category_id"})   // 한 직원은 한 게시판에 한 번만 권한 가짐
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardPermission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 게시판 권한 ID (PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee; // 사원 ID (FK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_category_id", nullable = false)
    private BoardCategory boardCategory; // 게시판 카테고리 ID (FK)

}