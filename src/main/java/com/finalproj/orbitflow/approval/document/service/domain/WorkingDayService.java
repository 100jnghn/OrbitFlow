package com.finalproj.orbitflow.approval.document.service.domain;

import java.time.LocalDate;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : WorkingDayService
 * @since : 26. 1. 6. 화요일
 **/


public interface WorkingDayService {

    List<LocalDate> getWorkingDates(
            LocalDate start,
            LocalDate end
    );
}
