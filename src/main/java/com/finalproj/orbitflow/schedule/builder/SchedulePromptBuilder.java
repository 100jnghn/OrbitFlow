package com.finalproj.orbitflow.schedule.builder;

import com.finalproj.orbitflow.schedule.dto.ScheduleSummaryReqDto;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.*;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : SchedulePromptBuilder
 * @since : 2025-12-30 오후 8:07 화요일
 */
@Slf4j
public class SchedulePromptBuilder {
    public static String buildDailySummaryPrompt(
            LocalDate today,
            List<ScheduleSummaryReqDto> dailySchedules
    ) {
        StringBuilder sb = new StringBuilder();

        // 1. System + 규칙
        sb.append("""
                너는 직장인의 주간 일정 요약 비서다.
                아래 규칙을 엄격히 준수해서 이번 주 일정을 요약하라.
                
                규칙:
                - 주어진 일정만 근거로 작성할 것
                - 추측하거나 없는 정보를 만들지 말 것
                - 시간 흐름이 드러나도록 요약할 것
                - 시작날짜와 종료 날짜가 다른 경우 종료 날짜를 정확히 명시할 것
                - '전사', '조직', '개인' 일정 별로 확실하게 일정을 구분하고 출력하고 줄바꿈할 것
                - 음슴체를 사용해서 최대 5문장 이내로 간결하게 작성할 것
                
                """);

        // 2. 날짜
        sb.append("날짜: ").append(today).append("\n\n");

        // 3. startAt ASC 정렬
        List<ScheduleSummaryReqDto> sorted =
                dailySchedules.stream()
                        .sorted(Comparator.comparing(ScheduleSummaryReqDto::getStartAt))
                        .toList();

        // 4. 분류
        List<ScheduleSummaryReqDto> companySchedules = new ArrayList<>();
        List<ScheduleSummaryReqDto> personalSchedules = new ArrayList<>();
        List<ScheduleSummaryReqDto> orgSchedules = new ArrayList<>();

        for (ScheduleSummaryReqDto dto : sorted) {
            if ("전사".equals(dto.getOrganizationName())) {
                companySchedules.add(dto);

            } else if ("개인".equals(dto.getOrganizationName())) {
                personalSchedules.add(dto);

            } else {
                orgSchedules.add(dto); // 그 외 조직
            }
        }

        appendSection(sb, companySchedules, "전사");
        appendSection(sb, orgSchedules, "조직", true);
        appendSection(sb, personalSchedules, "개인");

        return sb.toString();
    }


    public static String buildWeeklySummaryPrompt(
            LocalDate weekStart,
            List<ScheduleSummaryReqDto> weeklyReqDtos
    ) {
        StringBuilder sb = new StringBuilder();

        LocalDate weekEnd = weekStart.plusDays(6);

        sb.append("""
                너는 직장인의 주간 일정 요약 비서다.
                아래 규칙을 엄격히 준수해서 이번 주 일정을 요약하라.
                
                규칙:
                - 주어진 일정만 근거로 작성할 것
                - 추측하거나 없는 정보를 만들지 말 것
                - 이번 주 전체 흐름을 먼저 요약해서 첫 줄에 출력할 것
                - 이후 날짜별 일정을 자연스럽게 반영해
                - 시작일, 종료일, 시작 시간, 종료 시간을 정확히 명시할 것
                - "일정 없음"은 출력하지 말 것
                - 요일 정보는 한글로 출력할 것
                - '전사', '조직', '개인' 일정 별로 확실하게 일정을 구분하고 줄바꿈할 것
                - '음슴체'를 사용해서 최대 10문장 이내로 간결하게 작성할 것
                
                """);

        sb.append("기간: ")
                .append(weekStart)
                .append(" ~ ")
                .append(weekEnd)
                .append("\n\n");

        // startAt ASC 정렬
        List<ScheduleSummaryReqDto> sorted = weeklyReqDtos == null ? List.of()
                : weeklyReqDtos.stream()
                .sorted(Comparator.comparing(ScheduleSummaryReqDto::getStartAt))
                .toList();

        // 날짜별 그룹핑
        Map<LocalDate, List<ScheduleSummaryReqDto>> byDate = new LinkedHashMap<>();
        for (ScheduleSummaryReqDto dto : sorted) {
            LocalDate date = dto.getStartAt().toLocalDate();
            byDate.computeIfAbsent(date, k -> new ArrayList<>()).add(dto);
        }

        // 월~일까지 전부 출력 (일정 없으면 '일정 없음')
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            List<ScheduleSummaryReqDto> dailySchedules = byDate.getOrDefault(date, List.of());

            sb.append("[")
                    .append(date)
                    .append(" (")
                    .append(date.getDayOfWeek())
                    .append(")]\n");

            if (dailySchedules.isEmpty()) {
                sb.append("- 일정 없음\n\n");
                continue;
            }

            // 전사 / 개인 / 조직
            List<ScheduleSummaryReqDto> companySchedules = new ArrayList<>();
            List<ScheduleSummaryReqDto> personalSchedules = new ArrayList<>();
            List<ScheduleSummaryReqDto> orgSchedules = new ArrayList<>();

            for (ScheduleSummaryReqDto dto : dailySchedules) {
                String orgName = dto.getOrganizationName();

                if ("전사".equals(orgName)) {
                    companySchedules.add(dto);

                } else if ("개인".equals(orgName)) {
                    personalSchedules.add(dto);

                } else {
                    orgSchedules.add(dto);
                }
            }

            appendSection(sb, companySchedules, "전사");
            appendSection(sb, personalSchedules, "개인");
            appendSection(sb, orgSchedules, "조직", true);
        }

        return sb.toString();
    }

    private static void appendSection(
            StringBuilder sb,
            List<ScheduleSummaryReqDto> schedules,
            String title
    ) {
        appendSection(sb, schedules, title, false);
    }

    private static void appendSection(
            StringBuilder sb,
            List<ScheduleSummaryReqDto> schedules,
            String title,
            boolean showOrgName
    ) {
        if (schedules == null || schedules.isEmpty()) {
            return;
        }

        sb.append("[").append(title).append("]\n");

        for (ScheduleSummaryReqDto s : schedules) {
            sb.append("- ");

            if (showOrgName) {
                sb.append("[")
                        .append(s.getOrganizationName())
                        .append("] ");
            }

            sb.append(s.getStartAt().toLocalTime())
                    .append("~")
                    .append(s.getEndAt().toLocalTime())
                    .append(" ")
                    .append(s.getTitle())
                    .append("\n");
        }

        sb.append("\n");
    }


}
