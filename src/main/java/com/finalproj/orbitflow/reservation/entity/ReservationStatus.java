package com.finalproj.orbitflow.reservation.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.reservation.enums.ReservationStatusCode;
import jakarta.persistence.*;
import lombok.*;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ReservationStatus
 * @since : 2025-12-16 오후 1:05 화요일
 */
@Entity
@Table(name = "reservation_status")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReservationStatus {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status_code", length = 50)
    private ReservationStatusCode reservationStatusCode;

    @Column(name = "status_name", nullable = false, length = 50)
    private String statusName;
}