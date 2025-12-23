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

    // 회의실(MEETING), 차량(CAR), 비품(ITEM) 등 예약 종류
    @Enumerated(EnumType.STRING)
    @Column(name = "type_code", nullable = false, length = 20)
    private ReservationTypeCode typeCode;

    // 비품 예약일 경우에만 사용 (Nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_category_id")
    private ItemCategory itemCategory;

    // 다형성 ID: typeCode에 따라 회의실ID, 차량ID, 비품ID가 들어감
    // 따라서 연관관계 매핑 없이 단순 Long 값으로 저장
    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    // 시간은 정수형(예: 14)으로 관리
    @Column(name = "start_time", nullable = false)
    private Integer startTime;

    @Column(name = "end_time", nullable = false)
    private Integer endTime;

    @Column(name = "reservation_reason", nullable = false, length = 255)
    private String reservationReason;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_status_id")
    private ReservationStatus reservationStatus;

    // 예약 취소
    public void changeStatus(ReservationStatus reservationStatus) {
        this.reservationStatus = reservationStatus;
    }
}