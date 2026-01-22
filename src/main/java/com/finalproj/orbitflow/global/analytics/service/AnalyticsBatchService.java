package com.finalproj.orbitflow.global.analytics.service;

import com.finalproj.orbitflow.global.analytics.entity.CompanyDailyAiUsage;
import com.finalproj.orbitflow.global.analytics.entity.CompanyDailySnapshot;
import com.finalproj.orbitflow.global.analytics.repository.CompanyDailyAiUsageRepository;
import com.finalproj.orbitflow.global.analytics.repository.CompanyDailySnapshotRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsBatchService {

    private final EntityManager em;
    private final CompanyDailySnapshotRepository snapshotRepository;
    private final CompanyDailyAiUsageRepository aiUsageRepository;

    @Scheduled(cron = "0 0/10 * * * *") // 매 10분마다 당일 데이터 집계
    @Transactional
    public void runRecentAggregation() {
        runDailyAggregationInternal(LocalDate.now());
    }

    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void runDailyAggregation() {
        runDailyAggregationInternal(LocalDate.now().minusDays(1));
    }

    @Transactional
    public void runDailyAggregationInternal(LocalDate date) {
        log.info("Starting analytics aggregation for date: {}", date);
        aggregateSnapshots(date);
        aggregateAiUsage(date);
        log.info("Finished analytics aggregation.");
    }

    private void aggregateSnapshots(LocalDate date) {
        // 해당 날짜의 기존 데이터가 있으면 삭제 (재집계 대응)
        snapshotRepository.deleteBySnapshotDate(date);

        String sql = "SELECT " +
                "    c.id as company_id, " +
                "    (SELECT COUNT(*) FROM employee e WHERE e.company_id = c.id AND e.status = 'ACTIVE') as employee_count, "
                +
                "    (SELECT COUNT(*) FROM file f WHERE f.company_id = c.id AND DATE(f.created_at) = :date) as file_count, "
                +
                "    (SELECT IFNULL(SUM(f.file_size), 0) FROM file f WHERE f.company_id = c.id AND DATE(f.created_at) = :date) as file_bytes "
                +
                "FROM company c";

        Query query = em.createNativeQuery(sql);
        query.setParameter("date", java.sql.Date.valueOf(date));
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        for (Object[] row : results) {
            Long companyId = ((Number) row[0]).longValue();
            Integer empCount = ((Number) row[1]).intValue();
            Integer fileCount = ((Number) row[2]).intValue();
            Long fileBytes = ((Number) row[3]).longValue();

            CompanyDailySnapshot snapshot = CompanyDailySnapshot.builder()
                    .snapshotDate(date)
                    .companyId(companyId)
                    .employeeCount(empCount)
                    .fileCount(fileCount)
                    .fileBytes(fileBytes)
                    .activeYn("Y")
                    .build();

            snapshotRepository.save(snapshot);
        }
    }

    private void aggregateAiUsage(LocalDate date) {
        // 해당 날짜의 기존 데이터가 있으면 삭제 (재집계 대응)
        aiUsageRepository.deleteByUsageDate(date);

        String sql = "SELECT " +
                "    c.id as company_id, " +
                "    (SELECT COUNT(*) FROM schedule_summary ss WHERE ss.company_id = c.id AND DATE(ss.created_at) = :date) as schedule_cnt, "
                +
                "    (SELECT COUNT(*) FROM document_ai_summary ds WHERE ds.company_id = c.id AND ds.status = 'COMPLETED' AND ds.summary_type = 'CONTENT' AND DATE(ds.created_at) = :date) as doc_cnt, "
                +
                "    (SELECT COUNT(*) FROM document_ai_summary ds WHERE ds.company_id = c.id AND ds.status = 'COMPLETED' AND ds.summary_type = 'DIFF' AND DATE(ds.created_at) = :date) as compare_cnt, "
                +
                "    (SELECT COUNT(*) FROM log_form_template_ai la WHERE la.company_id = c.id AND la.status = 'COMPLETED' AND DATE(la.created_at) = :date) as outline_cnt, "
                +
                "    (SELECT COUNT(*) FROM chat_message cm WHERE cm.company_id = c.id AND cm.role = 'ASSISTANT' AND DATE(cm.created_at) = :date) as chatbot_cnt "
                +
                "FROM company c";

        Query query = em.createNativeQuery(sql);
        query.setParameter("date", java.sql.Date.valueOf(date));
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        for (Object[] row : results) {
            Long companyId = ((Number) row[0]).longValue();
            Integer scheduleCnt = ((Number) row[1]).intValue();
            Integer docCnt = ((Number) row[2]).intValue();
            Integer compareCnt = ((Number) row[3]).intValue();
            Integer outlineCnt = ((Number) row[4]).intValue();
            Integer chatbotCnt = ((Number) row[5]).intValue();
            Integer totalCnt = scheduleCnt + docCnt + compareCnt + outlineCnt + chatbotCnt;

            CompanyDailyAiUsage aiUsage = CompanyDailyAiUsage.builder()
                    .usageDate(date)
                    .companyId(companyId)
                    .scheduleSummaryCnt(scheduleCnt)
                    .docSummaryCnt(docCnt)
                    .compareSummaryCnt(compareCnt)
                    .outlineGenCnt(outlineCnt)
                    .chatbotCnt(chatbotCnt)
                    .totalCnt(totalCnt)
                    .build();

            aiUsageRepository.save(aiUsage);
        }
    }
}
