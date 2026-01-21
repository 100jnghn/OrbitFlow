package com.finalproj.orbitflow.approval.document.dto;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : TimeRange
 * @since : 26. 1. 21. 수요일
 **/


public record TimeRange(Instant start, Instant end) {

    public static TimeRange thisMonth() {
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime now = ZonedDateTime.now(zone);

        Instant start =
                now.withDayOfMonth(1)
                        .toLocalDate()
                        .atStartOfDay(zone)
                        .toInstant();

        return new TimeRange(start, Instant.now());
    }

    public static TimeRange lastMonth() {
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime now = ZonedDateTime.now(zone);

        Instant start =
                now.minusMonths(1)
                        .withDayOfMonth(1)
                        .toLocalDate()
                        .atStartOfDay(zone)
                        .toInstant();

        Instant end =
                now.withDayOfMonth(1)
                        .toLocalDate()
                        .atStartOfDay(zone)
                        .toInstant();

        return new TimeRange(start, end);
    }
}
