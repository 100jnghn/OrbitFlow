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

    @Transactional(readOnly = true) // 읽기 전용 트랜잭션 (성능 향상)
    public List<MeetingroomResDto> getMeetingrooms(Long companyId) {

        // DELETED는 가져오지 않음
        return meetingroomRepository.findAllByCompany_Id(companyId).stream()
                .map(this::convertToResDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MeetingroomResDto getMeetingroom(Long meetingroomId) {
        Meetingroom meetingroom = findMeetingroomById(meetingroomId);
        return convertToResDto(meetingroom);
    }

    @Transactional
    public void insertMeetingroom(Long companyId, MeetingroomReqDto dto) {

        Company company = companyRepository.getReferenceById(companyId); // Proxy 조회 (쿼리 절약)
        ResourceStatus status = findResourceStatus(dto.getStatusCode());

        Meetingroom meetingroom = Meetingroom.builder()
                .company(company)
                .name(dto.getName())
                .position(dto.getPosition())
                .description(dto.getDescription())
                .resourceStatus(status)
                .build();

        meetingroomRepository.save(meetingroom);
    }

    @Transactional
    public void updateMeetingroom(Long meetingroomId, MeetingroomReqDto dto) {

        Meetingroom meetingroom = findMeetingroomById(meetingroomId);
        ResourceStatus status = findResourceStatus(dto.getStatusCode());

        meetingroom.update(
                dto.getName(),
                dto.getPosition(),
                dto.getDescription(),
                status
        );
    }

    @Transactional
    public void deleteMeetingroom(Long meetingroomId) {

        Meetingroom meetingroom = findMeetingroomById(meetingroomId);

        ResourceStatus deleteStatus = resourceStatusRepository.findById(ResourceStatusCode.DELETED)
                .orElseThrow(() -> new IllegalStateException("삭제 상태 코드가 DB에 없습니다."));

        meetingroom.delete(deleteStatus);
    }


    // result dto로 변환
    private MeetingroomResDto convertToResDto(Meetingroom meetingroom) {
        String code = "ETC";
        String name = "기타";

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

    // 상태 코드 조회
    private ResourceStatus findResourceStatus(String statusCodeStr) {
        ResourceStatusCode statusCode;

        try {
            statusCode = ResourceStatusCode.valueOf(statusCodeStr);

        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("유효하지 않은 상태 코드입니다: " + statusCodeStr);
        }

        return resourceStatusRepository.findById(statusCode)
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 자원 상태입니다."));
    }

    // Meetingroom 찾기
    private Meetingroom findMeetingroomById(Long id) {
        return meetingroomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 회의실을 찾을 수 없습니다"));
    }

}