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
    private String objectKey;

    private String uploaderName;
    private LocalDate createdAt;



    // Entity -> DTO 변환 편의 메서드
//    public static CarResDto fromEntity(Car car) {
//        // 1. 상태값 처리 (Null 방지)
//        String code = "UNKNOWN";
//        String statusName = "알 수 없음";
//
//        if (car.getResourceStatus() != null) {
//            code = car.getResourceStatus().getResourceStatusCode().name();
//            statusName = car.getResourceStatus().getResourceStatusCode().getDescription(); // 혹은 getStatusName()
//        }
//
//        // 2. 파일 정보 처리 (Null 방지)
//        Long fileId = null;
//        String fileName = null;
//
//        if (car.getFile() != null) {
//            fileId = car.getFile().getId();
//            fileName = car.getFile().getOriginName(); // File 엔티티에 getOriginName()이 있다고 가정
//        }
//
//        return CarResDto.builder()
//                .carId(car.getId())
//                .number(car.getNumber())
//                .name(car.getName())
//                .driverAge(car.getDriverAge())
//                .description(car.getDescription())
//                .statusCode(code)
//                .statusName(statusName)
//                .fileId(fileId)
//                .fileName(fileName)
//                .build();
//    }
}