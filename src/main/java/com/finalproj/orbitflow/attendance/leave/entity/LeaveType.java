package com.finalproj.orbitflow.attendance.leave.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveType
 * @since : 2026. 1. 8. 목요일
 */


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
    private Long id;

    @Column(name = "type_name", nullable = false, length = 50)
    private String typeName;

    @Column(name = "is_countable", nullable = false)
    private Boolean isCountable;

    @Column(name = "description", length = 255)
    private String description;

    @Column(precision = 5, scale = 3)
    private BigDecimal unitDays;
}