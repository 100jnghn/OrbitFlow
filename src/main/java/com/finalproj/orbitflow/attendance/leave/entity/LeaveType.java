package com.finalproj.orbitflow.attendance.leave.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "leave_type")
public class LeaveType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id", nullable = false)
    private Long typeId;            // 타입 ID (PK)

    // leave_type 필드는 테이블 정의상 BIGINT이지만, ENUM 등으로 관리될 가능성이 있음
    @Column(name = "leave_type", nullable = false)
    private Long leaveType;         // 휴가 카테고리 (유형 코드)

    @Column(name = "type_name", nullable = false, length = 50)
    private String typeName;        // 카테고리 이름

    @Column(name = "is_countable", nullable = false)
    private Boolean isCountable;    // 잔여 일수 차감 여부 (일수가 차감되는 연차인지 여부)

    @Column(name = "description", length = 255)
    private String description;     // 상세 설명
}