package com.finalproj.orbitflow.resource.meetingroom.dto;

import com.finalproj.orbitflow.resource.enums.ResourceStatusCode;
import lombok.*;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : MeetingroomResDto
 * @since : 2025-12-16 오후 2:11 화요일
 */
@Getter
@Builder
@AllArgsConstructor
public class MeetingroomResDto {
    private Long meetingroomId;
    private String name;
    private String position;
    private String description;
    private String statusCode;
    private String statusName;
}