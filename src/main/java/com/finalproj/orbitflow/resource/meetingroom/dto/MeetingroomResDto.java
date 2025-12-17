package com.finalproj.orbitflow.resource.meetingroom.dto;

import com.finalproj.orbitflow.resource.enums.ResourceStatusCode;
import com.finalproj.orbitflow.resource.meetingroom.entity.Meetingroom;
import lombok.*;

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

    // 코드값 (프론트 로직 처리용) 예: "AVAILABLE"
    private String statusCode;

    // 화면 출력용 이름 예: "사용 가능"
    private String statusName;

    /**
     * Entity -> DTO 변환 편의 메서드 (Static Factory Method)
     * 이 메서드를 만들어두면 Service 코드가 매우 깔끔해집니다.
     */
    public static MeetingroomResDto fromEntity(Meetingroom meetingroom) {
        String code = ResourceStatusCode.ETC.toString();
        String name = "알 수 없음";

        // Null Safety 처리
        if (meetingroom.getResourceStatus() != null) {
            code = meetingroom.getResourceStatus().getResourceStatusCode().name();
            name = meetingroom.getResourceStatus().getResourceStatusCode().getDescription();
        }

        return MeetingroomResDto.builder()
                .meetingroomId(meetingroom.getId())
                .name(meetingroom.getName())
                .position(meetingroom.getPosition())
                .description(meetingroom.getDescription())
                .statusCode(code)
                .statusName(name)
                .build();
    }
}