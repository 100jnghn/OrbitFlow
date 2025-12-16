package com.finalproj.orbitflow.resource.meetingroom.dto;

import com.finalproj.orbitflow.resource.enums.ResourceStatusCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : MeetingroomReqDto
 * @since : 2025-12-16 오후 3:45 화요일
 */
@Getter
@Builder
@AllArgsConstructor
public class MeetingroomReqDto {
    private String name;
    private String position;
    private String description;
    private String statusCode;
    private String statusName;
}
