package com.finalproj.orbitflow.global.analytics.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsOverviewResDto {
    private Map<String, KpiDto> kpis;
    private List<TimeSeriesDataDto> series;
    private Map<String, List<Map<String, Object>>> top10;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class KpiDto {
        private String current;
        private String compare;
        private Double deltaPct;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeSeriesDataDto {
        private String label;
        private Map<String, Object> data;
    }
}
