package com.finalproj.orbitflow.reservation.dto;

import com.finalproj.orbitflow.reservation.enums.ReservationTypeCode;
import lombok.*;

import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ReservationResDto
 * @since : 2025-12-22 오후 2:27 월요일
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResDto {

    private Long reservationId;

    private String employeeName;
    private String organizationName;

    // MEETING // CAR // ITEM
    private ReservationTypeCode typeCode;
    private String typeName;

    private Long itemCategoryId;    // item 예약일 때만 사용
    private String itemCategoryName;

    private Long resourceId;        // MEETINGROOM // CAR // ITEM의 PK
    private String resourceName;

    private LocalDate reservationDate;  // 예약날짜
    private int startTime;
    private int endTime;

    private String reservationReason;
    private String rejectReason;

    /**
     * 1 - 승인 대기
     * 2 - 예약 확정
     * 3 - 예약 반려
     * 4 - 예약 취소
     * 5 - 삭제됨
     * 6 - 기타
     */
    private Long reservationStatusId;
    private String reservationStatusName;
}
