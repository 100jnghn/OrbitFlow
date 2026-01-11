package com.finalproj.orbitflow.schedule.aiModel;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : SummaryModel
 * @since : 2025-12-30 오후 9:42 화요일
 */
public interface SummaryModel {
    String summarizeDaily(String prompt);
    String summarizeWeekly(String prompt);
}
