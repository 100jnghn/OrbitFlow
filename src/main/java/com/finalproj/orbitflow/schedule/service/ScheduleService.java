package com.finalproj.orbitflow.schedule.service;

import com.finalproj.orbitflow.hr.organization.repository.OrgRepository;
import com.finalproj.orbitflow.hr.organization.repository.OrgResView;
import com.finalproj.orbitflow.schedule.dto.ScheduleReqDto;
import com.finalproj.orbitflow.schedule.dto.ScheduleResDto;
import com.finalproj.orbitflow.schedule.entity.Schedule;
import com.finalproj.orbitflow.schedule.enums.ScheduleStatus;
import com.finalproj.orbitflow.schedule.mapper.ScheduleMapper;
import com.finalproj.orbitflow.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ScheduleService
 * @since : 2025-12-27 오후 4:30 토요일
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final OrgRepository orgRepository;

    // 전사 일정 조회
    @Transactional(readOnly = true)
    public List<ScheduleResDto> getCompanySchedules(
            Long companyId,
            String status,
            int year,
            int month,
            boolean isWeekly
    ) {

        List<Schedule> schedules;

        // 월 단위 조회
        if (!isWeekly) {

            // 월 시작일
            LocalDateTime startOfMonth = LocalDate.of(year, month, 1).atStartOfDay();

            // 월 종료일
            LocalDateTime endOfMonth = LocalDate.of(year, month, 1)
                    .plusMonths(1)
                    .atStartOfDay()
                    .minusNanos(1);

            ScheduleStatus scheduleStatus;

            // 전체 ["RELEASE", "HOLD"] 조회
            if (status.equals("ALL")) {
                schedules = scheduleRepository.findMonthlyCompanySchedules(companyId, startOfMonth, endOfMonth);
            }
            // ['RELEASE", "HOLD"] 중 하나로 필터링
            else {
                scheduleStatus = ScheduleStatus.valueOf(status.toUpperCase());
                schedules = scheduleRepository.findMonthlyCompanySchedulesByStatus(companyId, scheduleStatus, startOfMonth, endOfMonth);
            }
        }
        // 주 단위 조회 - 사용자 일정 조회에서 사용
        else {
            // 오늘 날짜부터 7일
            LocalDateTime today = LocalDate.now().atStartOfDay();
            LocalDateTime endDay = today.plusDays(7);

            ScheduleStatus scheduleStatus = ScheduleStatus.RELEASE;

            schedules = scheduleRepository.findWeeklyCompanySchedules(companyId, scheduleStatus, today, endDay);
        }

        return schedules.stream().map(ScheduleMapper::toDto).toList();
    }

    // 조직 일정 검색에 사용
    @Transactional(readOnly = true)
    public List<ScheduleResDto> getOrganizationSchedules(
            Long companyId,
            int year,
            int month,
            List<Long> orgIds,
            boolean isWeekly
    ) {
        List<Schedule> schedules;

        // 월 단위 검색
        if (!isWeekly) {
            // month의 시작일
            LocalDateTime startOfMonth = LocalDate.of(year, month, 1).atStartOfDay();

            // moth의 종료일
            LocalDateTime endOfMonth = LocalDate.of(year, month, 1)
                    .plusMonths(1)
                    .atStartOfDay()
                    .minusNanos(1);

            schedules = scheduleRepository.findMonthlyOrganizationSchedules(
                    companyId,
                    ScheduleStatus.RELEASE, // 사용자 - 일정 조회는 RELEASE인 일정만 조회
                    orgIds,
                    startOfMonth,
                    endOfMonth
            );
        }
        // 주 단위 검색
        else {
            LocalDateTime today = LocalDate.now().atStartOfDay();
            LocalDateTime endDay = today.plusDays(7);

            ScheduleStatus scheduleStatus = ScheduleStatus.RELEASE;

            schedules = scheduleRepository.findWeeklyOrganizationSchedules(
                    companyId,
                    scheduleStatus,
                    orgIds,
                    today,
                    endDay
            );
        }

        return schedules.stream().map(ScheduleMapper::toDto).toList();
    }

    // 개인 일정 조회
    @Transactional(readOnly = true)
    public List<ScheduleResDto> getEmployeeSchedules(
            Long companyId,
            Long employeeId,
            int year,
            int month,
            boolean isWeekly
    ) {
        List<Schedule> schedules;
        ScheduleStatus scheduleStatus = ScheduleStatus.RELEASE;

        // 월 단위 검색
        if (!isWeekly) {
            // month의 시작일
            LocalDateTime startOfMonth = LocalDate.of(year, month, 1).atStartOfDay();

            // moth의 종료일
            LocalDateTime endOfMonth = LocalDate.of(year, month, 1)
                    .plusMonths(1)
                    .atStartOfDay()
                    .minusNanos(1);

            schedules = scheduleRepository.findMonthlyEmployeeSchedules(
                    companyId,
                    scheduleStatus,
                    employeeId,
                    startOfMonth,
                    endOfMonth
            );
        }
        // 주 단위 검색
        else {
            LocalDateTime today = LocalDate.now().atStartOfDay();
            LocalDateTime endDay = today.plusDays(7);

            schedules = scheduleRepository.findWeeklyEmployeeSchedules(
                    companyId,
                    scheduleStatus,
                    employeeId,
                    today,
                    endDay
            );
        }

        return schedules.stream().map(ScheduleMapper::toDto).toList();
    }

    // 일정 세부 조회
    @Transactional(readOnly = true)
    public ScheduleResDto getSchedule(Long companyId, Long scheduleId) {

        Schedule schedule = scheduleRepository.findByIdAndCompanyId(scheduleId, companyId);
        return ScheduleMapper.toDto(schedule);
    }

    // 해당 날짜 일정 조회
    // 개인 + 전사 + 소속 조직들 일정 조회
    @Transactional(readOnly = true)
    public List<ScheduleResDto> getDateSchedules(Long companyId, Long employeeId, Long orgId, LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        ScheduleStatus scheduleStatus = ScheduleStatus.RELEASE;

        // 소속 조직들 일정 조회
        List<Long> orgIds = orgRepository.findHierarchy(orgId)
                .stream()
                .filter(v -> v.getIsActive() != null && v.getIsActive() == 1) // 활성 조직만
                .map(OrgResView::getId)
                .toList();

        List<Schedule> orgSchedules = orgIds.isEmpty() ? List.of()
                : scheduleRepository.findDateOrganizationSchedules(
                companyId,
                scheduleStatus,
                orgIds,
                startOfDay,
                endOfDay
        );

        // 전사 일정 조회
        List<Schedule> companySchedules = scheduleRepository.findDateCompanySchedules(
                companyId,
                scheduleStatus,
                startOfDay,
                endOfDay
        );

        // 개인 일정 조회
        List<Schedule> employeeSchedules = scheduleRepository.findDateEmployeeSchedules(
                companyId,
                scheduleStatus,
                employeeId,
                startOfDay,
                endOfDay
        );

        // 일정 병합 + 정렬
        List<Schedule> schedules = Stream
                .of(companySchedules, orgSchedules, employeeSchedules)
                .flatMap(List::stream)
                .sorted(Comparator.comparing(Schedule::getStartAt))
                .toList();

        return schedules.stream().map(ScheduleMapper::toDto).toList();
    }

    // DELETE는 존재 여부에 대한 판단이므로 Repository에서 진행
    @Transactional
    public void deleteSchedule(Long companyId, Long scheduleId) {

        int deletedCount = scheduleRepository.deleteByIdAndCompanyId(scheduleId, companyId);

        if (deletedCount == 0) {
            throw new IllegalArgumentException("일정 삭제 실패.");
        }
    }

    // UPDATE는 상태 변경에 대한 판단이므로 Entity에서 진행
    @Transactional
    public void updateSchedule(Long companyId, Long scheduleId, ScheduleReqDto scheduleReqDto) {

        Schedule schedule = scheduleRepository.findByIdAndCompanyId(scheduleId, companyId);

        schedule.update(scheduleReqDto);
    }

    @Transactional
    public void insertSchedule(Long companyId, Long employeeId, ScheduleReqDto dto) {

        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException("일정 제목은 필수입니다.");
        }

        if (dto.getStartAt() == null || dto.getEndAt() == null) {
            throw new IllegalArgumentException("시작/종료 시간은 필수입니다.");
        }

        if (dto.getEndAt().isBefore(dto.getStartAt())) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 이후여야 합니다.");
        }

        Schedule schedule = ScheduleMapper.toEntity(companyId, employeeId, dto);
        scheduleRepository.save(schedule);
    }

}
