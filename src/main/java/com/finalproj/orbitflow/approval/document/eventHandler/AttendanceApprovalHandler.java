package com.finalproj.orbitflow.approval.document.eventHandler;

import com.finalproj.orbitflow.approval.attendanceEvent.entity.AttendanceEvent;
import com.finalproj.orbitflow.approval.attendanceEvent.repository.AttendanceEventRepository;
import com.finalproj.orbitflow.approval.document.dto.CommonPayload;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.documentContent.entity.DocumentContent;
import com.finalproj.orbitflow.approval.documentContent.repository.DocumentContentRepository;
import com.finalproj.orbitflow.approval.formTemplateGroup.enums.BaseRole;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.organization.entity.Organization;
import com.finalproj.orbitflow.hr.organization.repository.OrgRepository;
import com.finalproj.orbitflow.schedule.dto.ScheduleReqDto;
import com.finalproj.orbitflow.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AttendanceApprovalHandler
 * @since : 25. 12. 31. 수요일
 **/

@Component
@RequiredArgsConstructor
public class AttendanceApprovalHandler {


    private final DocumentRepository documentRepository;
    private final DocumentContentRepository documentContentRepository;
    private final DocumentContentParser documentContentParser;
    private final ScheduleService scheduleService;
    private final OrgRepository orgRepository;
    private final AttendanceEventRepository attendanceEventRepository;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(Long documentId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow();

        BaseRole baseRole = document.getTemplateGroup().getBaseRole();

        // 출장 / 외근만 처리
        if (baseRole != BaseRole.BUSINESS_TRIP &&
                baseRole != BaseRole.OUTWORK) {
            return;
        }

        DocumentContent content = documentContentRepository
                .findByDocument_Id(documentId)
                .orElseThrow();

        CommonPayload payload =
                documentContentParser.extractCommon(content);

        Employee writer = document.getWriter();

        Organization org = orgRepository
                .findFirstByCompanyIdAndParentOrgId(
                        writer.getCompany().getId(), null
                )
                .orElseThrow(() -> new NotFoundException("작성자의 최상위 조직 조회 실패"));

        String title = switch (baseRole) {
            case BUSINESS_TRIP -> "출장";
            case OUTWORK -> "외근";
            default -> payload.title();
        };
        
        /* =========================
           1. 캘린더 일정 생성
        ========================= */

        ScheduleReqDto scheduleReqDto = ScheduleReqDto.builder()
                .isCompany(true)
                .isPersonal(true)
                .orgCategoryId(org.getCategoryId())
                .orgId(org.getId())
                .title(title)
                .description(payload.description())
                .startAt(payload.startDate().atStartOfDay())
                .endAt(payload.endDate().atTime(23, 59, 59))
                .status("RELEASE")
                .build();

        scheduleService.newTransactionInsertSchedule(
                writer.getCompany().getId(),
                writer.getId(),
                scheduleReqDto
        );

        /* =========================
           2. 근태 반영용 기록 저장
        ========================= */
        attendanceEventRepository.save(
                AttendanceEvent.builder()
                        .employee(writer)
                        .company(writer.getCompany())
                        .baseRole(baseRole) // BUSINESS_TRIP / OUTWORK
                        .startDate(payload.startDate())
                        .endDate(payload.endDate())
                        .sourceDocument(document)
                        .build()
        );

    }
}
