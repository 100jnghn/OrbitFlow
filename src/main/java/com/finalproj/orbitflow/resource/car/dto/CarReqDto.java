package com.finalproj.orbitflow.resource.car.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : CarReqDto
 * @since : 2025-12-16 오후 6:53 화요일
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarReqDto {

    private String number;
    private String name;
    private Integer driverAge;
    private String description;

    private Long statusId;

    // 첨부 이미지
    private MultipartFile imgFile;
}