package com.finalproj.orbitflow.resource.car.dto;

import com.finalproj.orbitflow.global.file.entity.File;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : CarReqDto
 * @since : 2025-12-16 오후 6:53 화요일
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarReqDto {

    private String number;
    private String name;
    private Integer driverAge;
    private String description;

    // 상태 코드 (String으로 받아서 Service에서 Enum으로 변환)
    private String resourceStatusCode;

    // 첨부 이미지 ID (파일 업로드 후 반환받은 ID를 여기에 담아서 보냄)
    private MultipartFile imgFile;
}