package com.finalproj.orbitflow.resource.car.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : CarResDto
 * @since : 2025-12-16 오후 6:53 화요일
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarResDto {

    private Long carId;
    private String number;
    private String name;
    private Integer driverAge;
    private String description;

    private Long statusId;
    private String statusCode;
    private String statusName;

    // 이미지 파일 정보 (없으면 null)
    private Long fileId;

    private String uploaderName;
    private LocalDate createdAt;

}