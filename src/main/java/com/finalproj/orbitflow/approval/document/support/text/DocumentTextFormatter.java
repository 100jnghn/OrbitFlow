package com.finalproj.orbitflow.approval.document.support.text;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentTextFormatter
 * @since : 26. 1. 21. 수요일
 **/

public final class DocumentTextFormatter {

    private DocumentTextFormatter() {
    }

    public static String shortenTitle(String title, int maxLength) {
        if (title == null) return "";
        return title.length() <= maxLength
                ? title
                : title.substring(0, maxLength) + "...";
    }

    public static String now() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
