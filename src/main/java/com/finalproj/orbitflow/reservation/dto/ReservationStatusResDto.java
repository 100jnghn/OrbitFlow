package com.finalproj.orbitflow.reservation.dto;

import com.finalproj.orbitflow.reservation.entity.ReservationStatus;
import lombok.*;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ReservationStatusResDto
 * @since : 2025-12-22 오후 2:28 월요일
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationStatusResDto {

    private Long id;
    private String statusCode;
    private String statusName;

    // Entity -> Dto
    public static ReservationStatusResDto fromEntity(ReservationStatus reservationStatus) {
        return ReservationStatusResDto.builder()
                .id(reservationStatus.getId())
                .statusCode(reservationStatus.getStatusCode().name())
                .statusName(reservationStatus.getStatusName())
                .build();
    }
}
