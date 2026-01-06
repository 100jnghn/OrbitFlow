package com.finalproj.orbitflow.approval.document.service;

import com.finalproj.orbitflow.approval.document.dto.LeaveCalculationResult;
import com.finalproj.orbitflow.approval.document.dto.VacationPayload;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.event.DocumentContentParser;
import com.finalproj.orbitflow.approval.documentContent.entity.DocumentContent;
import com.finalproj.orbitflow.approval.documentContent.repository.DocumentContentRepository;
import com.finalproj.orbitflow.attendance.leave.entity.LeaveType;
import com.finalproj.orbitflow.attendance.leave.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : LeaveCalculationService
 * @since : 26. 1. 6. 화요일
 **/


@Service
@RequiredArgsConstructor
public class LeaveCalculationService {

    private final DocumentContentRepository documentContentRepository;
    private final DocumentContentParser documentContentParser;
    private final LeaveTypeRepository leaveTypeRepository;

    public LeaveCalculationResult calculate(Document document) {

        DocumentContent content = documentContentRepository
                .findByDocument_Id(document.getId())
                .orElseThrow();

        VacationPayload payload =
                documentContentParser.extractVacation(content);

        LeaveType leaveType = leaveTypeRepository
                .findById(payload.vacationTypeId())
                .orElseThrow();

        BigDecimal days;

        if (leaveType.getUnitDays().compareTo(BigDecimal.ONE) < 0) {
            days = leaveType.getUnitDays();
        } else {
            long dayCount = ChronoUnit.DAYS.between(
                    payload.startDate(),
                    payload.endDate()
            ) + 1;

            days = leaveType.getUnitDays()
                    .multiply(BigDecimal.valueOf(dayCount));
        }

        return new LeaveCalculationResult(payload, leaveType, days);
    }
}
