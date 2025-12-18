package com.finalproj.orbitflow.resource.meetingroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : MeetingroomReqDto
 * @since : 2025-12-16 오후 3:45 화요일
 */
@Getter
@NoArgsConstructor // JSON 파싱을 위해 필수
@AllArgsConstructor
@Builder
public class MeetingroomReqDto {

    private String name;
    private String position;
    private String description;

    private Long statusId;
}