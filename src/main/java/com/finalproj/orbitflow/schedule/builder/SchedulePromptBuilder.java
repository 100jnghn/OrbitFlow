package com.finalproj.orbitflow.schedule.builder;

import com.finalproj.orbitflow.schedule.dto.ScheduleSummaryReqDto;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : SchedulePromptBuilder
 * @since : 2025-12-30 오후 8:07 화요일
 */
public class SchedulePromptBuilder {

    // 일일 요약 프롬프트 작성
    public static String buildDailySummaryPrompt(
            LocalDate today,
            List<ScheduleSummaryReqDto> dailySchedules
    ) {
        StringBuilder sb = new StringBuilder();

        // 1. System + 규칙 (User prompt에 같이 포함)
        sb.append("""
                너는 직장인의 일정 요약 비서다.
                아래 일정을 바탕으로 하루 일정을 요약하라.
                
                규칙:
                - 주어진 일정만 근거로 작성할 것
                - 추측하거나 없는 정보를 만들지 말 것
                - 시간 흐름이 드러나도록 요약할 것
                - '전사', '조직', '개인' 일정 별로 구분할 것
                - 높임말을 사용해서 최대 5문장 이내로 간결하게 작성할 것
                
                """);

        // 2. 날짜
        sb.append("날짜: ").append(today).append("\n\n");

        // 3. startAt ASC 정렬
        List<ScheduleSummaryReqDto> sorted =
                dailySchedules.stream()
                        .sorted(Comparator.comparing(
                                ScheduleSummaryReqDto::getStartAt
                        ))
                        .toList();

        // 4. organizationName 기준 분류
        Map<String, List<ScheduleSummaryReqDto>> grouped =
                sorted.stream()
                        .collect(Collectors.groupingBy(
                                ScheduleSummaryReqDto::getOrganizationName,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        // 5. 출력 순서 고정
        appendSection(sb, grouped, "전사", "전사 일정");
        appendSection(sb, grouped, "조직", "조직 일정", true);
        appendSection(sb, grouped, "개인", "개인 일정");

        return sb.toString();
    }

    public static String buildWeeklySummaryPrompt(
            LocalDate weekStart,
            List<ScheduleSummaryReqDto> weeklyReqDtos
    ) {
        StringBuilder sb = new StringBuilder();

        LocalDate weekEnd = weekStart.plusDays(6);

        // 1. System + 규칙
        sb.append("""
                너는 직장인의 주간 일정 요약 비서다.
                아래 일정을 바탕으로 이번 주 일정을 요약하라.
                
                규칙:
                - 주어진 일정만 근거로 작성할 것
                - 추측하거나 없는 정보를 만들지 말 것
                - 이번 주 전체 흐름을 먼저 요약할 것
                - 이후 날짜별 특징을 자연스럽게 반영할 것
                - '전사', '조직', '개인' 일정 별로 구분할 것
                - 높임말을 사용해서 최대 10문장 이내로 간결하게 작성할 것
                
                """);

        // 2. 주간 기간
        sb.append("기간: ")
                .append(weekStart)
                .append(" ~ ")
                .append(weekEnd)
                .append("\n\n");

        // 3. 날짜 → startAt ASC 정렬
        List<ScheduleSummaryReqDto> sorted =
                weeklyReqDtos.stream()
                        .sorted(Comparator.comparing(
                                ScheduleSummaryReqDto::getStartAt
                        ))
                        .toList();

        // 4. 날짜별 그룹핑
        Map<LocalDate, List<ScheduleSummaryReqDto>> byDate =
                sorted.stream()
                        .collect(Collectors.groupingBy(
                                dto -> dto.getStartAt().toLocalDate(),
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        // 5. 날짜별 출력
        for (Map.Entry<LocalDate, List<ScheduleSummaryReqDto>> entry : byDate.entrySet()) {

            LocalDate date = entry.getKey();
            List<ScheduleSummaryReqDto> dailySchedules = entry.getValue();

            sb.append("[")
                    .append(date)
                    .append(" (")
                    .append(date.getDayOfWeek())
                    .append(")]\n");

            // 날짜 내에서도 organizationName 기준 분류
            Map<String, List<ScheduleSummaryReqDto>> grouped =
                    dailySchedules.stream()
                            .collect(Collectors.groupingBy(
                                    ScheduleSummaryReqDto::getOrganizationName,
                                    LinkedHashMap::new,
                                    Collectors.toList()
                            ));

            appendSection(sb, grouped, "전사", "전사 일정");
            appendSection(sb, grouped, "조직", "조직 일정", true);
            appendSection(sb, grouped, "개인", "개인 일정");
        }

        return sb.toString();
    }


    // 섹션 출력 (조직명 포함 여부 선택)
    private static void appendSection(
            StringBuilder sb,
            Map<String, List<ScheduleSummaryReqDto>> grouped,
            String key,
            String title
    ) {
        appendSection(sb, grouped, key, title, false);
    }

    private static void appendSection(
            StringBuilder sb,
            Map<String, List<ScheduleSummaryReqDto>> grouped,
            String key,
            String title,
            boolean showOrgName
    ) {
        List<ScheduleSummaryReqDto> schedules = grouped.get(key);

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
