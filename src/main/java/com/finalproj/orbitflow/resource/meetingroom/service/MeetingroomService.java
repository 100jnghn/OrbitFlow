package com.finalproj.orbitflow.resource.meetingroom.service;

import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.resource.enums.ResourceStatusCode;
import com.finalproj.orbitflow.resource.meetingroom.dto.MeetingroomReqDto;
import com.finalproj.orbitflow.resource.meetingroom.dto.MeetingroomResDto;
import com.finalproj.orbitflow.resource.meetingroom.entity.Meetingroom;
import com.finalproj.orbitflow.resource.meetingroom.repository.MeetingroomRepository;
import com.finalproj.orbitflow.resource.status.entity.ResourceStatus;
import com.finalproj.orbitflow.resource.status.repository.ResourceStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
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
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final ResourceStatusRepository resourceStatusRepository;

    @Transactional(readOnly = true)
    public Page<MeetingroomResDto> getMeetingrooms(
            Long companyId,
            Pageable pageable
    ) {
        return meetingroomRepository
                .findAllByCompany_Id(companyId, pageable)
                .map(this::convertToResDto);
    }


    public List<MeetingroomResDto> getAvailableMeetingrooms(Long companyId) {

        // AVAILABLE만 조회
        return meetingroomRepository.findAllByCompanyIdAndStatus(companyId, ResourceStatusCode.AVAILABLE).stream()
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

        Company company = companyRepository.getReferenceById(companyId);
        ResourceStatus resourceStatus = findResourceStatus(dto.getStatusId());

        Meetingroom meetingroom = Meetingroom.builder()
                .company(company)
                .name(dto.getName())
                .position(dto.getPosition())
                .description(dto.getDescription())
                .resourceStatus(resourceStatus)
                .build();

        meetingroomRepository.save(meetingroom);
    }

    @Transactional
    public void updateMeetingroom(Long meetingroomId, MeetingroomReqDto dto) {

        Meetingroom meetingroom = findMeetingroomById(meetingroomId);
        ResourceStatus status = findResourceStatus(dto.getStatusId());

        meetingroom.update(
                dto.getName(),
                dto.getPosition(),
                dto.getDescription(),
                status
        );
    }

    @Transactional
    public void deleteMeetingroom(Long meetingroomId) {
        if (!meetingroomRepository.existsById(meetingroomId)) {
            throw new NotFoundException("이미 삭제된 회의실");
        }

        meetingroomRepository.deleteById(meetingroomId);
    }


    // result dto로 변환
    private MeetingroomResDto convertToResDto(Meetingroom meetingroom) {
        Long statusId = null;
        String code = "ETC";
        String name = "기타";

        if (meetingroom.getResourceStatus() != null) {
            statusId = meetingroom.getResourceStatus().getId();
            code = meetingroom.getResourceStatus().getResourceStatusCode().name();
            name = meetingroom.getResourceStatus().getResourceStatusCode().getDescription();
        }

        Employee uploader = employeeRepository.getReferenceById(meetingroom.getCreatedBy());
        String uploaderName = uploader.getName();

        LocalDate createdAt = meetingroom.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDate();


        return MeetingroomResDto.builder()
                .meetingroomId(meetingroom.getId())
                .name(meetingroom.getName())
                .position(meetingroom.getPosition())
                .description(meetingroom.getDescription())
                .statusId(statusId)
                .statusCode(code)
                .statusName(name)
                .uploaderName(uploaderName)
                .createdAt(createdAt)
                .build();
    }

    // 상태 코드 조회
    private ResourceStatus findResourceStatus(Long statusId) {

        return resourceStatusRepository.findById(statusId)
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 자원 상태입니다."));
    }

    // Meetingroom 찾기
    private Meetingroom findMeetingroomById(Long id) {
        return meetingroomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 회의실을 찾을 수 없습니다"));
    }


}