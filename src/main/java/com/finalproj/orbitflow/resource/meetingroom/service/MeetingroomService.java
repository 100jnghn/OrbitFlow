package com.finalproj.orbitflow.resource.meetingroom.service;

import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.resource.enums.ResourceStatusCode;
import com.finalproj.orbitflow.resource.meetingroom.dto.MeetingroomReqDto;
import com.finalproj.orbitflow.resource.meetingroom.dto.MeetingroomResDto;
import com.finalproj.orbitflow.resource.meetingroom.entity.Meetingroom;
import com.finalproj.orbitflow.resource.meetingroom.repository.MeetingroomRepository;
import com.finalproj.orbitflow.resource.status.entity.ResourceStatus;
import com.finalproj.orbitflow.resource.status.repository.ResourceStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.View;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : MeetingroomService
 * @since : 2025-12-16 오후 2:20 화요일
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MeetingroomService {

    private final MeetingroomRepository meetingroomRepository;
    private final CompanyRepository companyRepository;
    private final ResourceStatusRepository resourceStatusRepository;
    private final View error;

    public List<MeetingroomResDto> getMeetingrooms(Long companyId) {

        List<Meetingroom> meetingrooms = meetingroomRepository.findAllByCompany_Id(companyId);

        return meetingrooms.stream()
                .map(meetingroom -> MeetingroomResDto.builder()
                        .meetingroomId(meetingroom.getId())
                        .name(meetingroom.getName())
                        .position(meetingroom.getPosition())
                        .description(meetingroom.getDescription())
                        .statusCode(
                                meetingroom.getResourceStatus().getResourceStatusCode().toString()
                        )
                        .statusName(
                                meetingroom.getResourceStatus().getResourceStatusCode().getDescription()
                        )
                        .build()
                )
                .collect(Collectors.toList());
    }

    public MeetingroomResDto getMeetingroom(Long meetingroomId) {
        Meetingroom meetingroom = meetingroomRepository.findById(meetingroomId)
                .orElseThrow(() -> new IllegalArgumentException("회의실이 존재하지 않습니다"));

        ResourceStatus status = meetingroom.getResourceStatus();

        return MeetingroomResDto.builder()
                .meetingroomId(meetingroom.getId())
                .name(meetingroom.getName())
                .position(meetingroom.getPosition())
                .description(meetingroom.getDescription())
                .statusCode(status.getResourceStatusCode().toString())
                .statusName(status.getResourceStatusCode().getDescription())
                .build();
    }

}
