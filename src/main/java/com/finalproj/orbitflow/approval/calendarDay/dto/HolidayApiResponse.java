package com.finalproj.orbitflow.approval.calendarDay.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : HolidayApiResponse
 * @since : 26. 1. 6. 화요일
 **/


@Getter
public class HolidayApiResponse {

    private List<Item> items;

    @Getter
    public static class Item {

        /**
         * yyyyMMdd
         */
        private String locdate;

        private String dateName;

        public LocalDate toLocalDate() {
            return LocalDate.parse(
                    locdate,
                    DateTimeFormatter.BASIC_ISO_DATE
            );
        }

        public String getName() {
            return dateName;
        }
    }
}