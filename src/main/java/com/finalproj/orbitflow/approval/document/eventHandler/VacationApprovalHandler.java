package com.finalproj.orbitflow.approval.document.eventHandler;

import com.finalproj.orbitflow.approval.document.dto.VacationPayload;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.documentContent.entity.DocumentContent;
import com.finalproj.orbitflow.approval.documentContent.repository.DocumentContentRepository;
import com.finalproj.orbitflow.approval.formTemplateGroup.enums.BaseRole;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.organization.entity.Organization;
import com.finalproj.orbitflow.hr.organization.repository.OrgRepository;
import com.finalproj.orbitflow.attendance.leave.entity.LeaveType;
import com.finalproj.orbitflow.attendance.leave.repository.LeaveTypeRepository;
import com.finalproj.orbitflow.schedule.dto.ScheduleReqDto;
import com.finalproj.orbitflow.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : VacationApprovalHandler
 * @since : 25. 12. 31. 수요일
 **/


@Component
@RequiredArgsConstructor
@Slf4j
public class VacationApprovalHandler {

    private final DocumentRepository documentRepository;
    private final DocumentContentRepository documentContentRepository;
    private final DocumentContentParser documentContentParser;
    private final OrgRepository orgRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final ScheduleService scheduleService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(Long documentId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow();

        if (document.getTemplateGroup().getBaseRole() != BaseRole.VACATION) {
            return;
        }

        DocumentContent content = documentContentRepository
                .findByDocument_Id(documentId)
                .orElseThrow();

        VacationPayload payload =
                documentContentParser.extractVacation(content);

        Employee writer = document.getWriter();


        Organization org = orgRepository.findFirstByCompanyIdAndParentOrgId(writer.getCompany().getId(), null)
                .orElseThrow(() -> new NotFoundException("작성자의 최상위 조직 조회 실패"));


        LeaveType leave = leaveTypeRepository.findById(payload.vacationTypeId())
                .orElseThrow(() -> new NotFoundException("휴가 유형 이름 조회 실패"));

        ScheduleReqDto scheduleReqDto = ScheduleReqDto.builder()
                .isCompany(true)
                .orgCategoryId(org.getCategoryId())
                .orgId(org.getId())
                .title(leave.getTypeName())
                .description(payload.reason())
                .startAt(payload.startDate().atStartOfDay())
                .endAt(payload.endDate().atTime(23, 59, 59))
                .status("RELEASE")
                .build();

        scheduleService.newTransactionInsertSchedule(writer.getCompany().getId(), writer.getId(), scheduleReqDto);

    }
}
