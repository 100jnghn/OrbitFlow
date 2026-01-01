package com.finalproj.orbitflow.approval.document.eventHandler;

import com.finalproj.orbitflow.approval.document.dto.CommonPayload;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.documentContent.entity.DocumentContent;
import com.finalproj.orbitflow.approval.documentContent.repository.DocumentContentRepository;
import com.finalproj.orbitflow.approval.formTemplateGroup.enums.BaseRole;
import com.finalproj.orbitflow.attendance.default_rule.entity.AttendanceRule;
import com.finalproj.orbitflow.attendance.default_rule.repository.AttendanceRuleRepository;
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

import java.time.LocalTime;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : CompanyEventApprovalHandler
 * @since : 25. 12. 31. 수요일
 **/

@Component
@RequiredArgsConstructor
public class CompanyEventApprovalHandler {

    private final DocumentRepository documentRepository;
    private final DocumentContentRepository documentContentRepository;
    private final DocumentContentParser documentContentParser;
    private final ScheduleService scheduleService;
    private final OrgRepository orgRepository;
    private final AttendanceRuleRepository attendanceRuleRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(Long documentId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));

        // COMPANY_EVENT가 아니면 무시
        if (document.getTemplateGroup().getBaseRole() != BaseRole.COMPANY_EVENT) {
            return;
        }

        DocumentContent content = documentContentRepository
                .findByDocument_Id(document.getId())
                .orElseThrow(() ->
                        new NotFoundException("문서 내용을 찾을 수 없습니다.")
                );

        CommonPayload payload =
                documentContentParser.extractCommon(content);

        Employee writer = document.getWriter();


        Organization org = orgRepository.findFirstByCompanyIdAndParentOrgId(writer.getCompany().getId(), null)
                .orElseThrow(() -> new NotFoundException("작성자의 최상위 조직 조회 실패"));

        AttendanceRule rule = attendanceRuleRepository.findByCompanyIdAndIsDefaultTrue(writer.getCompany().getId()).orElseThrow(() -> new NotFoundException("회사의 출퇴근 시간 조회 실패"));

        LocalTime st = rule.getDefaultStartTime();
        LocalTime et = rule.getDefaultEndTime();

        ScheduleReqDto scheduleReqDto = ScheduleReqDto.builder()
                .isCompany(true)
                .orgCategoryId(org.getCategoryId())
                .title(payload.title())
                .description(payload.description())
                .startAt(payload.startDate().atTime(st.getHour(), st.getMinute(), st.getSecond()))
                .endAt(payload.endDate().atTime(et.getHour(), et.getMinute(), et.getSecond()))
                .status("RELEASE")
                .build();

        scheduleService.newTransactionInsertSchedule(writer.getCompany().getId(), writer.getId(), scheduleReqDto);
    }
}
