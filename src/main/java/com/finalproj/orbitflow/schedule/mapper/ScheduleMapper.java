package com.finalproj.orbitflow.schedule.mapper;

import com.finalproj.orbitflow.schedule.dto.ScheduleReqDto;
import com.finalproj.orbitflow.schedule.dto.ScheduleResDto;
import com.finalproj.orbitflow.schedule.entity.Schedule;
import com.finalproj.orbitflow.schedule.enums.ScheduleStatus;

/**
 * entity to dto
 *
 * @author : 종훈
 * @filename : ScheduleMapper
 * @since : 2025-12-27 오후 4:40 토요일
 */
public final class ScheduleMapper {

    private ScheduleMapper() {}

    // entity -> res dto
    public static ScheduleResDto toDto(Schedule schedule) {
        return ScheduleResDto.builder()
                .scheduleId(schedule.getId())
                .companyId(schedule.getCompanyId())
                .isCompany(schedule.isCompany())
                .isPersonal(schedule.isPersonal())
                .orgCategoryId(schedule.getOrgCategoryId())
                .orgId(schedule.getOrgId())
                .employeeId(schedule.getEmployeeId())
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .startAt(schedule.getStartAt())
                .endAt(schedule.getEndAt())
                .status(schedule.getStatus())
                .build();
    }

    // req dto -> entity
    public static Schedule toEntity(
            Long companyId,
            Long employeeId,
            ScheduleReqDto dto
    ) {
        // 만약 req dto의 status가 null이면 RELEASE로 입력
        ScheduleStatus status = ScheduleStatus.RELEASE;

        if (dto.getStatus() != null) {
            status = ScheduleStatus.valueOf(dto.getStatus().toUpperCase());
        }

        return Schedule.builder()
                .companyId(companyId)
                .isCompany(dto.isCompany())
                .isPersonal(dto.isPersonal())
                .employeeId(employeeId)
                .orgCategoryId(dto.getOrgCategoryId())
                .orgId(dto.getOrgId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startAt(dto.getStartAt())
                .endAt(dto.getEndAt())
                .status(status)
                .build();
    }
}