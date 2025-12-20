package com.finalproj.orbitflow.resource.meetingroom.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.resource.meetingroom.dto.MeetingroomReqDto;
import com.finalproj.orbitflow.resource.meetingroom.dto.MeetingroomResDto;
import com.finalproj.orbitflow.resource.meetingroom.service.MeetingroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : MeetingRoomController
 * @since : 2025-12-16 오후 1:48 화요일
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MeetingRoomController {

    private final MeetingroomService meetingroomService;

    // 관리자 - 회의실 리스트 조회
    @GetMapping("/admin/meetingrooms")
    public ResponseEntity<ResponseDto> getMeetingrooms(@AuthenticationPrincipal SecurityUser user) {

        Long companyId = user.getCompanyId();
        List<MeetingroomResDto> meetingrooms = meetingroomService.getMeetingrooms(companyId);

        return ResponseEntity.ok()
                .body(new ResponseDto(HttpStatus.OK, "회의실 목록 조회 성공", meetingrooms));
    }

    // 사용자 - 회의실 조회 (상태가 AVAILABLE인 회의실만 조회)
    @GetMapping("/meetingrooms")
    public ResponseEntity<ResponseDto> getAvailableMeetingrooms(@AuthenticationPrincipal SecurityUser user) {
        Long companyId = user.getCompanyId();
        List<MeetingroomResDto> meetingrooms = meetingroomService.getAvailableMeetingrooms(companyId);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "회의실 목록 조회 성공", meetingrooms)
        );
    }

    // 관리자 | 사용자 - 회의실 상세 조회
    @GetMapping("/meetingrooms/{meetingroomId}")
    public ResponseEntity<ResponseDto> getMeetingroom(@PathVariable Long meetingroomId) {

        MeetingroomResDto meetingroom = meetingroomService.getMeetingroom(meetingroomId);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "회의실 상세 조회 성공", meetingroom)
        );
    }

    // 관리자 - 회의실 등록
    @PostMapping("/admin/meetingrooms")
    public ResponseEntity<ResponseDto> insertMeetingroom(
            @RequestBody MeetingroomReqDto dto,
            @AuthenticationPrincipal SecurityUser user
    ) {
        Long companyId = user.getCompanyId();
        meetingroomService.insertMeetingroom(companyId, dto);

        return ResponseEntity.ok()
                .body(new ResponseDto(HttpStatus.OK, "회의실 등록 성공", null));
    }

    // 관리자 - 회의실 수정
    @PutMapping("/admin/meetingrooms/{meetingroomId}")
    public ResponseEntity<ResponseDto> updateMeetingroom(
            @PathVariable Long meetingroomId,
            @RequestBody MeetingroomReqDto dto
    ) {
        meetingroomService.updateMeetingroom(meetingroomId, dto);

        return ResponseEntity.ok()
                .body(new ResponseDto(HttpStatus.OK, "회의실 수정 성공", null));
    }

    // 관리자 - 회의실 삭제
    @PatchMapping("/admin/meetingrooms/{meetingroomId}/delete")
    public ResponseEntity<ResponseDto> deleteMeetingroom(
            @PathVariable Long meetingroomId
    ) {
        meetingroomService.deleteMeetingroom(meetingroomId);

        return ResponseEntity.ok()
                .body(new ResponseDto(HttpStatus.OK, "회의실 삭제 성공", null));
    }
}
