package com.finalproj.orbitflow.approval.document.service.domain;

import com.finalproj.orbitflow.approval.document.dto.LeaveCalculationResult;
import com.finalproj.orbitflow.approval.document.dto.VacationPayload;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.schema.DocumentContentParser;
import com.finalproj.orbitflow.approval.document.content.entity.DocumentContent;
import com.finalproj.orbitflow.approval.document.content.repository.DocumentContentRepository;
import com.finalproj.orbitflow.attendance.leave.entity.LeaveType;
import com.finalproj.orbitflow.attendance.leave.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
    private final WorkingDayService workingDayService;

    public LeaveCalculationResult calculate(Document document) {

        DocumentContent content = documentContentRepository
                .findByDocument_Id(document.getId())
                .orElseThrow();

        VacationPayload payload =
                documentContentParser.extractVacation(content);

        LeaveType leaveType = leaveTypeRepository
                .findById(payload.vacationTypeId())
                .orElseThrow();
        List<LocalDate> effectiveDates =
                workingDayService.getWorkingDates(
                        payload.startDate(),
                        payload.endDate()
                );
        if (effectiveDates == null || effectiveDates.isEmpty()) {
            throw new IllegalStateException(
                    "유효한 휴가 일자가 없습니다. start=" +
                            payload.startDate() + ", end=" + payload.endDate()
            );
        }
        BigDecimal days =
                BigDecimal.valueOf(effectiveDates.size())
                        .multiply(leaveType.getUnitDays());
        return new LeaveCalculationResult(
                payload,
                leaveType,
                days,
                effectiveDates
        );
    }

}
