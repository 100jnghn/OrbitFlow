package com.finalproj.orbitflow.approval.document.dto;

import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : VacationPayload
 * @since : 25. 12. 31. 수요일
 **/


public record VacationPayload(
        Long vacationTypeId,
        String reason,
        LocalDate startDate,
        LocalDate endDate
) {
}
