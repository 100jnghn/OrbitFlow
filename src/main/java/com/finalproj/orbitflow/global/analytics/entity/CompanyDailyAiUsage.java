package com.finalproj.orbitflow.global.analytics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "company_daily_ai_usage")
@IdClass(CompanyDailyAiUsageId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CompanyDailyAiUsage {

    @Id
    @Column(name = "usage_date")
    private LocalDate usageDate;

    @Id
    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "schedule_summary_cnt", nullable = false)
    private Integer scheduleSummaryCnt;

    @Column(name = "doc_summary_cnt", nullable = false)
    private Integer docSummaryCnt;

    @Column(name = "compare_summary_cnt", nullable = false)
    private Integer compareSummaryCnt;

    @Column(name = "outline_gen_cnt", nullable = false)
    private Integer outlineGenCnt;

    @Column(name = "chatbot_cnt", nullable = false)
    private Integer chatbotCnt;

    @Column(name = "total_cnt", nullable = false)
    private Integer totalCnt;
}
