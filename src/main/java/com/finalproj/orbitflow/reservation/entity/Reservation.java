package com.finalproj.orbitflow.reservation.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.reservation.enums.ReservationTypeCode;
import com.finalproj.orbitflow.resource.itemcategory.entity.ItemCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : Reservation
 * @since : 2025-12-16 오후 1:09 화요일
 */
@Entity
@Table(name = "reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_code", nullable = false, length = 20)
    private ReservationTypeCode typeCode;

    // Item 예약일 때만 값이 들어오므로 nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_category_id")
    private ItemCategory itemCategory;

    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    // 시간은 정수형 (8~20)
    @Column(name = "start_time", nullable = false)
    private Integer startTime;

    @Column(name = "end_time", nullable = false)
    private Integer endTime;

    @Column(name = "reservation_reason", nullable = false)
    private String reservationReason;

    @Column(name = "reject_reason")
    private String rejectReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_status_code", nullable = false)
    private ReservationStatus reservationStatus;
}