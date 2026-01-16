package com.finalproj.orbitflow.global.analytics.service;

import com.finalproj.orbitflow.global.analytics.dto.AnalyticsOverviewResDto;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final EntityManager em;
    private final AnalyticsBatchService batchService;
    private final CompanyRepository companyRepository;

    @Transactional
    public void syncNow(LocalDate date) {
        if (date != null) {
            batchService.runDailyAggregationInternal(date);
        } else {
            // 기본적으로 어제와 오늘 데이터를 모두 집계 (테스트 및 최신성 확보)
            batchService.runDailyAggregationInternal(LocalDate.now().minusDays(1));
            batchService.runDailyAggregationInternal(LocalDate.now());
        }
    }

    public AnalyticsOverviewResDto getOverview(String granularity, LocalDate from, LocalDate to, String compare,
            Long companyId) {
        // 1. 현재 기간 데이터 조회
        List<Map<String, Object>> currentRaw = fetchRawData(granularity, from, to, companyId);

        // 2. 비교 기간 데이터 조회 (필요시)
        List<Map<String, Object>> compareRaw = new ArrayList<>();
        if (!"none".equals(compare)) {
            LocalDate[] compareRange = calculateCompareRange(from, to, compare);
            compareRaw = fetchRawData(granularity, compareRange[0], compareRange[1], companyId);
        }

        // 3. KPI 계산
        Map<String, AnalyticsOverviewResDto.KpiDto> kpis = calculateKpis(currentRaw, compareRaw);

        // 4. 시계열 데이터 가공
        List<AnalyticsOverviewResDto.TimeSeriesDataDto> series = formatSeries(currentRaw, granularity);

        // 5. Top 10 데이터 조회
        Map<String, List<Map<String, Object>>> top10 = fetchTop10(from, to, companyId);

        return AnalyticsOverviewResDto.builder()
                .kpis(kpis)
                .series(series)
                .top10(top10)
                .build();
    }

    private List<Map<String, Object>> fetchRawData(String granularity, LocalDate from, LocalDate to,
            Long companyId) {
        String dateGroup = switch (granularity) {
            case "week" -> "DATE_FORMAT(s.snapshot_date, '%Y-%m-%d')";
            case "month" -> "DATE_FORMAT(s.snapshot_date, '%Y-%m-%d')";
            case "year" -> "DATE_FORMAT(s.snapshot_date, '%Y-%m')";
            default -> {
                long diffDays = java.time.temporal.ChronoUnit.DAYS.between(from, to);
                if (diffDays <= 60)
                    yield "DATE_FORMAT(s.snapshot_date, '%Y-%m-%d')";
                else
                    yield "DATE_FORMAT(s.snapshot_date, '%Y-%m')";
            }
        };

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
                .append(dateGroup).append(" as label, ")
                .append("COUNT(DISTINCT s.company_id) as company_cnt, ")
                .append("SUM(s.employee_count) / COUNT(DISTINCT s.snapshot_date) as avg_emp_cnt, ")
                .append("SUM(a.total_cnt) as ai_total, ")
                .append("SUM(a.schedule_summary_cnt) as ai_schedule, ")
                .append("SUM(a.doc_summary_cnt) as ai_doc, ")
                .append("SUM(a.compare_summary_cnt) as ai_compare, ")
                .append("SUM(a.outline_gen_cnt) as ai_outline, ")
                .append("SUM(a.chatbot_cnt) as ai_chatbot, ")
                .append("SUM(s.file_count) as file_cnt, ")
                .append("SUM(s.file_bytes) as file_bytes ")
                .append("FROM company_daily_snapshot s ")
                .append("LEFT JOIN company_daily_ai_usage a ON s.snapshot_date = a.usage_date AND s.company_id = a.company_id ")
                .append("WHERE s.snapshot_date BETWEEN :from AND :to ");

        if (companyId != null && companyId > 0) {
            sql.append("AND s.company_id = :companyId ");
        }

        sql.append("GROUP BY label ORDER BY label ASC");

        Query query = em.createNativeQuery(sql.toString());
        query.setParameter("from", java.sql.Date.valueOf(from));
        query.setParameter("to", java.sql.Date.valueOf(to));
        if (companyId != null && companyId > 0) {
            query.setParameter("companyId", companyId);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        List<Map<String, Object>> data = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("label", row[0] != null ? row[0].toString() : "");
            map.put("company_cnt", row[1] != null ? ((Number) row[1]).longValue() : 0L);
            map.put("emp_cnt", row[2] != null ? ((Number) row[2]).longValue() : 0L);
            map.put("ai_total", row[3] != null ? ((Number) row[3]).longValue() : 0L);
            map.put("ai_schedule", row[4] != null ? ((Number) row[4]).longValue() : 0L);
            map.put("ai_doc", row[5] != null ? ((Number) row[5]).longValue() : 0L);
            map.put("ai_compare", row[6] != null ? ((Number) row[6]).longValue() : 0L);
            map.put("ai_outline", row[7] != null ? ((Number) row[7]).longValue() : 0L);
            map.put("ai_chatbot", row[8] != null ? ((Number) row[8]).longValue() : 0L);
            map.put("file_cnt", row[9] != null ? ((Number) row[9]).longValue() : 0L);
            map.put("file_bytes", row[10] != null ? ((Number) row[10]).longValue() : 0L);
            data.add(map);
        }
        return data;
    }

    private LocalDate[] calculateCompareRange(LocalDate from, LocalDate to, String compare) {
        if ("prev_year".equals(compare)) {
            return new LocalDate[] { from.minusYears(1), to.minusYears(1) };
        } else {
            long days = java.time.temporal.ChronoUnit.DAYS.between(from, to) + 1;
            return new LocalDate[] { from.minusDays(days), to.minusDays(days) };
        }
    }

    private Map<String, AnalyticsOverviewResDto.KpiDto> calculateKpis(List<Map<String, Object>> current,
            List<Map<String, Object>> compare) {
        Map<String, AnalyticsOverviewResDto.KpiDto> kpis = new LinkedHashMap<>();

        kpis.put("companyCount", createKpi(current, compare, "company_cnt", false));
        kpis.put("employeeCount", createKpi(current, compare, "emp_cnt", false));
        kpis.put("fileCount", createKpi(current, compare, "file_cnt", true));
        kpis.put("fileBytes", createKpi(current, compare, "file_bytes", true));
        kpis.put("aiUsage", createKpi(current, compare, "ai_total", true));

        return kpis;
    }

    private AnalyticsOverviewResDto.KpiDto createKpi(List<Map<String, Object>> current,
            List<Map<String, Object>> compare, String key, boolean isSum) {
        long curVal = isSum ? current.stream().mapToLong(m -> (long) m.get(key)).sum()
                : (current.isEmpty() ? 0 : (long) current.get(current.size() - 1).get(key));
        long cmpVal = isSum ? compare.stream().mapToLong(m -> (long) m.get(key)).sum()
                : (compare.isEmpty() ? 0 : (long) compare.get(compare.size() - 1).get(key));

        double delta = 0.0;
        if (cmpVal > 0) {
            delta = ((double) (curVal - cmpVal) / cmpVal) * 100;
        }

        return AnalyticsOverviewResDto.KpiDto.builder()
                .current(String.valueOf(curVal))
                .compare(String.valueOf(cmpVal))
                .deltaPct(Math.round(delta * 10.0) / 10.0)
                .build();
    }

    private List<AnalyticsOverviewResDto.TimeSeriesDataDto> formatSeries(List<Map<String, Object>> raw,
            String granularity) {
        List<AnalyticsOverviewResDto.TimeSeriesDataDto> series = new ArrayList<>();
        for (Map<String, Object> map : raw) {
            series.add(AnalyticsOverviewResDto.TimeSeriesDataDto.builder()
                    .label((String) map.get("label"))
                    .data(map)
                    .build());
        }
        return series;
    }

    private Map<String, List<Map<String, Object>>> fetchTop10(LocalDate from, LocalDate to, Long companyId) {
        Map<String, List<Map<String, Object>>> top10 = new HashMap<>();

        // 특정 회사 선택 시 해당 회사의 데이터만 보여줌 (Top 10은 무의미할 수 있으므로 필터링된 1개만 나옴)
        String companyFilter = (companyId != null && companyId > 0) ? " AND c.id = :companyId " : "";

        // 1. AI 사용량 Top 10
        String aiSql = "SELECT c.name, SUM(a.total_cnt) as total " +
                "FROM company_daily_ai_usage a " +
                "JOIN company c ON a.company_id = c.id " +
                "WHERE a.usage_date BETWEEN :from AND :to " + companyFilter +
                " GROUP BY c.id, c.name ORDER BY total DESC LIMIT 10";
        top10.put("ai", fetchTopN(aiSql, from, to, companyId));

        // 2. 파일 용량 Top 10
        String fileSql = "SELECT c.name, MAX(s.file_bytes) as total " +
                "FROM company_daily_snapshot s " +
                "JOIN company c ON s.company_id = c.id " +
                "WHERE s.snapshot_date BETWEEN :from AND :to " + companyFilter +
                " GROUP BY c.id, c.name ORDER BY total DESC LIMIT 10";
        top10.put("file", fetchTopN(fileSql, from, to, companyId));

        return top10;
    }

    private List<Map<String, Object>> fetchTopN(String sql, LocalDate from, LocalDate to, Long companyId) {
        Query query = em.createNativeQuery(sql);
        query.setParameter("from", java.sql.Date.valueOf(from));
        query.setParameter("to", java.sql.Date.valueOf(to));
        if (companyId != null && companyId > 0) {
            query.setParameter("companyId", companyId);
        }
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", row[0]);
            map.put("value", ((Number) row[1]).longValue());
            list.add(map);
        }
        return list;
    }

    public List<Map<String, Object>> getCompanyList() {
        return companyRepository.findAll().stream()
                .map(c -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", c.getId());
                    m.put("name", c.getName());
                    m.put("businessNumber", c.getBusinessNumber());
                    return m;
                })
                .collect(Collectors.toList());
    }
}
