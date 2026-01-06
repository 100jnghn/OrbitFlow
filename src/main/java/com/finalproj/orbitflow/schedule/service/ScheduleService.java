package com.finalproj.orbitflow.schedule.service;

import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.hr.organization.repository.OrgRepository;
import com.finalproj.orbitflow.hr.organization.repository.OrgResView;
import com.finalproj.orbitflow.notification.enums.NotificationType;
import com.finalproj.orbitflow.notification.service.NotificationCommandService;
import com.finalproj.orbitflow.schedule.dto.ScheduleReqDto;
import com.finalproj.orbitflow.schedule.dto.ScheduleResDto;
import com.finalproj.orbitflow.schedule.entity.Schedule;
import com.finalproj.orbitflow.schedule.enums.ScheduleStatus;
import com.finalproj.orbitflow.schedule.mapper.ScheduleMapper;
import com.finalproj.orbitflow.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationCommandService notificationCommandService;

    // 전사 일정 조회
    @Transactional(readOnly = true)
    public List<ScheduleResDto> getCompanySchedules(
            Long companyId,
            String status,
            int year,
            int month
    ) {

        List<Schedule> schedules;

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
            schedules = scheduleRepository.findCompanySchedules(companyId, startOfMonth, endOfMonth);
        }
        // ['RELEASE", "HOLD"] 중 하나로 필터링
        else {
            scheduleStatus = ScheduleStatus.valueOf(status.toUpperCase());
            schedules = scheduleRepository.findCompanySchedulesByStatus(companyId, scheduleStatus, startOfMonth, endOfMonth);
        }

        return schedules.stream().map(ScheduleMapper::toDto).toList();
    }

    // 사용자 - 전사 일정 조회
    @Transactional(readOnly = true)
    public List<ScheduleResDto> getUserCompanySchedule(Long companyId, int year, int month, boolean isWeekly) {

        List<Schedule> schedules;
        LocalDateTime startOfDate;
        LocalDateTime endOfDate;
        ScheduleStatus scheduleStatus = ScheduleStatus.RELEASE;

        // 월 단위
        if (!isWeekly) {
            startOfDate = LocalDate.of(year, month, 1).atStartOfDay();
            endOfDate = LocalDate.of(year, month, 1)
                    .plusMonths(1)
                    .atStartOfDay()
                    .minusNanos(1);
        }
        // 주 단위 (일정 요약에 사용) (내일 날짜부터 +7일)
        else {
            startOfDate = LocalDate.now().atStartOfDay().plusDays(1);
            endOfDate = startOfDate.plusDays(7);
        }

        schedules = scheduleRepository.findUserCompanySchedules(companyId, startOfDate, endOfDate, scheduleStatus);
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
        LocalDateTime startOfDate;
        LocalDateTime endOfDate;
        ScheduleStatus scheduleStatus = ScheduleStatus.RELEASE;

        // 월 단위 검색
        if (!isWeekly) {
            startOfDate = LocalDate.of(year, month, 1).atStartOfDay();
            endOfDate = LocalDate.of(year, month, 1)
                    .plusMonths(1)
                    .atStartOfDay()
                    .minusNanos(1);

        }
        // 주 단위 검색
        else {
            startOfDate = LocalDate.now().atStartOfDay();
            endOfDate = startOfDate.plusDays(7);
        }

        schedules = scheduleRepository.findOrganizationSchedules(
                companyId,
                scheduleStatus,
                orgIds,
                startOfDate,
                endOfDate
        );
        return schedules.stream().map(ScheduleMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleResDto> getDateOrganizationSchedules(Long companyId, List<Long> orgIds, LocalDate date) {

        List<Schedule> schedules;

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        ScheduleStatus scheduleStatus = ScheduleStatus.RELEASE;

        schedules = scheduleRepository.findDateOrganizationSchedules(
                companyId,
                scheduleStatus,
                orgIds,
                startOfDay,
                endOfDay
        );

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
        LocalDateTime startOfDate;
        LocalDateTime endOfDate;
        ScheduleStatus scheduleStatus = ScheduleStatus.RELEASE;

        // 월 단위 검색
        if (!isWeekly) {
            startOfDate = LocalDate.of(year, month, 1).atStartOfDay();
            endOfDate = LocalDate.of(year, month, 1)
                    .plusMonths(1)
                    .atStartOfDay()
                    .minusNanos(1);

        }
        // 주 단위 검색
        else {
            startOfDate = LocalDate.now().atStartOfDay();
            endOfDate = startOfDate.plusDays(7);
        }

        schedules = scheduleRepository.findEmployeeSchedules(
                companyId,
                scheduleStatus,
                employeeId,
                startOfDate,
                endOfDate
        );

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

        // 전사 + 개인 일정 (휴가, 출장 등)
        List<Schedule> companyEmployeeSchedules = scheduleRepository.findDateCompanyEmployeeSchedules(
                companyId,
                scheduleStatus,
                employeeId,
                startOfDay,
                endOfDay
        );

        // 일정 병합 + 정렬
        List<Schedule> schedules = Stream
                .of(companySchedules, orgSchedules, employeeSchedules, companyEmployeeSchedules)
                .flatMap(List::stream)
                .sorted(Comparator.comparing(Schedule::getStartAt))
                .toList();

        return schedules.stream().map(ScheduleMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleResDto> getCompanyEmployeeSchedules(Long companyId, Long employeeId, int year, int month, boolean isWeekly) {

        List<Schedule> schedules;
        LocalDateTime startOfDate;
        LocalDateTime endOfDate;
        ScheduleStatus scheduleStatus = ScheduleStatus.RELEASE;

        // 월 단위
        if (!isWeekly) {
            startOfDate = LocalDate.of(year, month, 1).atStartOfDay();
            endOfDate = LocalDate.of(year, month, 1)
                    .plusMonths(1)
                    .atStartOfDay()
                    .minusNanos(1);
        }
        // 주 단위 (일정 요약에 사용) (내일 날짜부터 +7일)
        else {
            startOfDate = LocalDate.now().atStartOfDay().plusDays(1);
            endOfDate = startOfDate.plusDays(7);
        }

        schedules = scheduleRepository.findCompanyEmployeeSchedules(companyId, employeeId, startOfDate, endOfDate, scheduleStatus);
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

        // region exception
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException("일정 제목은 필수입니다.");
        }

        if (dto.getStartAt() == null || dto.getEndAt() == null) {
            throw new IllegalArgumentException("시작/종료 시간은 필수입니다.");
        }

        if (dto.getEndAt().isBefore(dto.getStartAt())) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 이후여야 합니다.");
        }
        // endregion

        Schedule schedule = ScheduleMapper.toEntity(companyId, employeeId, dto);
        scheduleRepository.save(schedule);

        // 알림 전송 - 전사
        if (schedule.isCompany() && !schedule.isPersonal()) {
            createCompanyNotification(schedule);
        }
        // 알림 전송 - 조직
        else if (!schedule.isCompany() && !schedule.isPersonal()) {
            createOrganizationNotification(schedule);
        }
    }

    // 전사 일정 알림 - 전사 직원들에게
    private void createCompanyNotification(Schedule schedule) {

        Long companyId = schedule.getCompanyId();
        List<Long> companyEmployeeIds = employeeRepository.findEmployeeIdsByCompanyId(companyId);

        String notificationMessage = createNotificationMessage(schedule, "전사");

        // 전사 직원들에게 알림 전송
        for (Long employeeId : companyEmployeeIds) {

            // 알림 생성 서비스 호출
            notificationCommandService.createNotification(
                    companyId,
                    employeeId,
                    NotificationType.SCHEDULE,
                    notificationMessage,
                    "/view/schedule"
            );
        }
    }

    // 조직 일정 알림 - 소속 조직 직원들에게
    private void createOrganizationNotification(Schedule schedule) {

        Long companyId = schedule.getCompanyId();
        Long orgId = schedule.getOrgId();
        List<Long> orgEmployeeIs = employeeRepository.findEmployeeIdsByOrganizationId(orgId);

        String notificationMessage = createNotificationMessage(schedule, "조직");

        // 조직 직원들에게 알림 전송
        for (Long employeeId : orgEmployeeIs) {

            // 알림 생성 서비스 호출
            notificationCommandService.createNotification(
                    companyId,
                    employeeId,
                    NotificationType.SCHEDULE,
                    notificationMessage,
                    "/view/schedule"
            );
        }
    }

    // 알림 메시지 만들기
    private String createNotificationMessage(Schedule schedule, String type) {

        String date = schedule.getStartAt().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        ) + " ~ " + schedule.getEndAt().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        );
        String msg = date + "\n" + schedule.getTitle() + " - " + schedule.getDescription();

        return msg;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void newTransactionInsertSchedule(Long companyId, Long employeeId, ScheduleReqDto dto) {
        insertSchedule(companyId, employeeId, dto);
    }

}
