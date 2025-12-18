package com.finalproj.orbitflow.resource.meetingroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : MeetingroomResDto
 * @since : 2025-12-16 오후 2:11 화요일
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingroomResDto {

    private Long meetingroomId;
    private String name;
    private String position;
    private String description;

    private Long statusId;
    private String statusCode;
    private String statusName;
}