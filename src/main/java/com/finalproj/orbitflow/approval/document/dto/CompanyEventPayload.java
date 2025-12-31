package com.finalproj.orbitflow.approval.document.dto;

import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : CompanyEventPayload
 * @since : 25. 12. 31. 수요일
 **/


public record CompanyEventPayload(
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate
) {
}
