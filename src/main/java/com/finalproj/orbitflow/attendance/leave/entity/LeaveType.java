package com.finalproj.orbitflow.attendance.leave.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "leave_type")
public class LeaveType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;            // 타입 ID (PK)

    @Column(name = "type_name", nullable = false, length = 50)
    private String typeName;        // 카테고리 이름

    @Column(name = "is_countable", nullable = false)
    private Boolean isCountable;    // 잔여 일수 차감 여부 (일수가 차감되는 연차인지 여부)

    @Column(name = "description", length = 255)
    private String description;     // 상세 설명
}