package com.finalproj.orbitflow.schedule.service;

import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.hr.organization.dto.OrgResDto;
import com.finalproj.orbitflow.hr.organization.service.OrgService;
import com.finalproj.orbitflow.schedule.aimodel.OpenAiSummaryModelService;
import com.finalproj.orbitflow.schedule.builder.SchedulePromptBuilder;
import com.finalproj.orbitflow.schedule.dto.ScheduleResDto;
import com.finalproj.orbitflow.schedule.dto.ScheduleSummaryReqDto;
import com.finalproj.orbitflow.schedule.dto.ScheduleSummaryResDto;
import com.finalproj.orbitflow.schedule.entity.ScheduleSummary;
import com.finalproj.orbitflow.schedule.repository.ScheduleSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ScheduleSummaryService
 * @since : 2025-12-30 오후 5:22 화요일
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduleSummaryService {

    private static final Duration COOL_TIME = Duration.ofMinutes(1);    // 60분

    private final ScheduleSummaryRepository scheduleSummaryRepository;
    private final OpenAiSummaryModelService aiService;

    private final ScheduleService scheduleService;
    private final OrgService orgService;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public ScheduleSummaryResDto getScheduleSummary(Long companyId, Long orgId, Long employeeId) {

        ScheduleSummaryResDto result = new ScheduleSummaryResDto();
        ScheduleSummary summaryRecord = scheduleSummaryRepository.findByEmployee_Id(employeeId).orElse(null);

        // 새로운 사용자가 요약 시도 -> 요약 실행
        if (summaryRecord == null) {
            log.info("새로운 사용자 요약");
            result = doSummary(companyId, orgId, employeeId, false);
        }
        // 기존 사용자가 요약 시도 + 시간 충족 -> 요약 실행
        else if (summaryRecord != null && getCoolTime(employeeId)) {
            log.info("기존 요약 업데이트 시도");
            result = doSummary(companyId, orgId, employeeId, true);
        }
        // 기존 사용자가 요약 시도 + 시간 불충족 -> 기존 데이터 반환
        else {
            log.info("시간 불충족. 업데이트 패스");
            ScheduleSummary scheduleSummary = scheduleSummaryRepository.findByEmployee_Id(employeeId).get();
            result.setDailySummary(scheduleSummary.getDailySummary());
            result.setWeeklySummary(scheduleSummary.getWeeklySummary());
        }

        return result;
    }

    // 시간 충족하면 true 반환
    private boolean getCoolTime(Long employeeId) {

        Instant updatedAt = scheduleSummaryRepository.findByEmployee_Id(employeeId).get().getUpdatedAt();
        Instant now = Instant.now();

        log.info("현재 시간 : " + now);
        log.info("마지막 업데이트 시간 : " + updatedAt);

        return now.isAfter(updatedAt.plus(COOL_TIME));
    }

    // AI에게 전달할 ReqDto로 변환
    public List<ScheduleSummaryReqDto> buildSummaryReqDtos(
            Long companyId,
            List<ScheduleResDto> schedules
    ) {
        Set<Long> orgIds = schedules.stream()
                .filter(schedule -> !schedule.isCompany())  // 전사 일정 X
                .map(ScheduleResDto::getOrgId)
                .filter(Objects::nonNull)                                  // 조직 일정 O
                .collect(Collectors.toSet());

        // 회사의 Org 목록 조회, ID:이름 매핑
        List<OrgResDto> orgs = orgService.findAll(companyId, false);
        Map<Long, String> orgNames = orgs
                .stream()
                .collect(Collectors.toMap(
                        OrgResDto::getId,
                        OrgResDto::getName
                ));

        // 3. DTO 생성
        return schedules.stream()
                .map(schedule -> {
                    String organizationName = "개인";

                    if (schedule.isCompany() && !schedule.isPersonal()) {
                        organizationName = "전사";

                    } else if (!schedule.isCompany() && schedule.isPersonal()){
                        organizationName = "개인";

                    } else if (!schedule.isCompany() && !schedule.isPersonal()) {
                        organizationName = "조직";
                    }

                    return ScheduleSummaryReqDto.builder()
                            .title(schedule.getTitle())
                            .description(schedule.getDescription())
                            .startAt(schedule.getStartAt())
                            .endAt(schedule.getEndAt())
                            .organizationName(organizationName)
                            .build();
                })
                .toList();
    }

    // 요약 기능 수행
    private ScheduleSummaryResDto doSummary(Long companyId, Long orgId, Long employeeId, boolean isUpdate) {

        ScheduleSummaryResDto scheduleSummary = new ScheduleSummaryResDto();

        // DB에서 오늘 일정 가져오기
        LocalDate today = LocalDate.now();
        List<ScheduleResDto> dailySchedules = scheduleService.getDateSchedules(companyId, employeeId, orgId, today);

        // ReqDto로 변환
        List<ScheduleSummaryReqDto> dailyReqDtos = buildSummaryReqDtos(companyId, dailySchedules);


        // ----- 주간 일정 ----- //
        List<ScheduleResDto> weeklySchedules;

        List<ScheduleResDto> companySchedules = scheduleService.getUserCompanySchedule(
                companyId,
                today.getYear(),
                today.getMonthValue(),
                true
        );

        List<ScheduleResDto> employeeSchedules = scheduleService.getEmployeeSchedules(
                companyId,
                employeeId,
                today.getYear(),
                today.getMonthValue(),
                true
        );

        List<Long> userOrgs = orgService.findOrgsByOrgId(orgId).stream().map(OrgResDto::getId).collect(Collectors.toList());

        log.info("사용자 조직 트리 : " + userOrgs);

        List<ScheduleResDto> orgSchedules = scheduleService.getOrganizationSchedules(
                companyId,
                today.getYear(),
                today.getMonthValue(),
                userOrgs,
                true
        );
        log.info("조직 일정 수 : " + orgSchedules.size());
        log.info("조직 일정 : " + orgSchedules);

        weeklySchedules = Stream
                .of(companySchedules, employeeSchedules, orgSchedules)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .sorted(Comparator.comparing(ScheduleResDto::getStartAt))
                .collect(Collectors.toList());

        // 주간 일정을 req dto로 변환
        List<ScheduleSummaryReqDto> weeklyReqDtos = buildSummaryReqDtos(companyId, weeklySchedules);


        // 일정이 없다면 요약 X
        if (dailySchedules.isEmpty()) {
            scheduleSummary.setDailySummary("오늘 일정이 없습니다.");

        } else {
            // AI에게 전달할 프롬프트 생성
            String dailySummaryPrompt = SchedulePromptBuilder.buildDailySummaryPrompt(today, dailyReqDtos);

            // 일일, 주간 프롬프트 전달 -> 요약
            String dailySummary = aiService.summarizeDaily(dailySummaryPrompt);
            scheduleSummary.setDailySummary(dailySummary);
        }

        if (weeklyReqDtos.isEmpty()) {
            scheduleSummary.setWeeklySummary("주간 일정이 없습니다");

        } else {
            String weeklySummaryPrompt = SchedulePromptBuilder.buildWeeklySummaryPrompt(today, weeklyReqDtos);
            String weeklySummary = aiService.summarizeWeekly(weeklySummaryPrompt);
            scheduleSummary.setWeeklySummary(weeklySummary);
        }

        // 새롭게 요약 생성
        Company company = companyRepository.getReferenceById(companyId);
        Employee employee = employeeRepository.getReferenceById(employeeId);


        // ----- 업데이트 or 저장 ----- //

        // 업데이트하는 경우 -> db 업데이트
        if (isUpdate) {
            ScheduleSummary summaryRecord = scheduleSummaryRepository.findByEmployee_Id(employeeId).get();
            summaryRecord.update(scheduleSummary.getDailySummary(), scheduleSummary.getWeeklySummary());

        }
        // 새로 만드는 경우 -> db INSERT
        else {
            ScheduleSummary newScheduleSummary = ScheduleSummary.builder()
                    .company(company)
                    .employee(employee)
                    .dailySummary(scheduleSummary.getDailySummary())
                    .weeklySummary(scheduleSummary.getWeeklySummary())
                    .build();

            scheduleSummaryRepository.save(newScheduleSummary);
        }

        return scheduleSummary;
    }

}
