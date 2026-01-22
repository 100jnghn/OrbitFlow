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
 * 휴가 결재 문서 기준으로 실제 차감 일수를 계산하는 도메인 서비스.
 * <p>
 * 휴가 문서에 입력된 기간과 휴가 유형을 기준으로
 * 근무일만을 추출한 뒤, 휴가 유형의 단위(unitDays)를 적용해
 * 최종 차감 일수와 적용 대상 날짜 목록을 계산한다.
 * <p>
 * 계산 결과는 LeaveCalculationResult로 반환되며,
 * 결재 상신 및 승인 이후 근태/휴가 도메인 처리에서 사용된다.
 *
 * @author Choi MinHyeok
 * @filename LeaveCalculationService
 * @since 2026.01.06
 */


@Service
@RequiredArgsConstructor
public class LeaveCalculationService {

    private final DocumentContentRepository documentContentRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final WorkingDayService workingDayService;

    private final DocumentContentParser documentContentParser;

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
