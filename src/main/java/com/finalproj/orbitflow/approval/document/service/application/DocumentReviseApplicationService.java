package com.finalproj.orbitflow.approval.document.service.application;

import com.finalproj.orbitflow.approval.line.service.ApprovalLineDomainService;
import com.finalproj.orbitflow.approval.document.dto.DocumentCreateResDto;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.document.support.access.DocumentAccessValidator;
import com.finalproj.orbitflow.approval.document.content.entity.DocumentContent;
import com.finalproj.orbitflow.approval.document.content.repository.DocumentContentRepository;
import com.finalproj.orbitflow.approval.document.file.entity.DocumentFile;
import com.finalproj.orbitflow.approval.document.file.enums.DocumentFileStatus;
import com.finalproj.orbitflow.approval.document.file.repository.DocumentFileRepository;
import com.finalproj.orbitflow.approval.form.template.entity.FormTemplate;
import com.finalproj.orbitflow.approval.form.template.repository.FormTemplateRepository;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentReviseApplicationService
 * @since : 26. 1. 21. 수요일
 **/


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentReviseApplicationService {

    private final EmployeeRepository employeeRepository;
    private final DocumentRepository documentRepository;
    private final DocumentContentRepository documentContentRepository;
    private final DocumentFileRepository documentFileRepository;
    private final FormTemplateRepository formTemplateRepository;
    private final ApprovalLineDomainService approvalLineDomainService;
    private final DocumentAccessValidator documentAccessValidator;

    @Transactional
    public DocumentCreateResDto revise(Long employeeId, Long documentId) {
        // 1. 작성자 조회
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("사원을 찾지 못했습니다."));

        // 2. 원본 문서 조회 (작성자 검증 포함)
        Document rejected = documentAccessValidator.getForWriter(employee, documentId);

        // 3. 상태 검증
        if (rejected.getStatus() != DocumentStatus.REJECTED) {
            throw new InvalidRequestException("반려 상태의 문서만 재기안할 수 있습니다.");
        }

        // 4. 이미 재기안 문서 존재 여부 확인
        boolean existsRevision =
                documentRepository.existsByBeforeDocument_Id(rejected.getId());

        if (existsRevision) {
            throw new InvalidRequestException("이미 재기안 문서가 존재합니다.");
        }

        // 5. 사용된 양식 조회 (당시 버전 기준)
        FormTemplate rejectedTemplate =
                formTemplateRepository
                        .findByTemplateGroup_idAndVersion(
                                rejected.getTemplateGroup().getId(),
                                rejected.getTemplateVersion()
                        )
                        .orElseThrow(() -> new NotFoundException("문서 양식 조회 실패"));

        // 6. 재기안 문서 생성 (DRAFT)
        Document revised = Document.reviseDraft(rejected);
        documentRepository.save(revised);

        // 기존 문서 첨부파일 조회
        List<DocumentFile> originalFiles =
                documentFileRepository.findByDocument_Id(rejected.getId());

        // DocumentFile만 복제 (File은 공유)
        for (DocumentFile df : originalFiles) {
            if (df.getStatus() == DocumentFileStatus.DELETED) continue;

            DocumentFile copied =
                    DocumentFile.copyFor(revised, df);

            documentFileRepository.save(copied);
        }


        // 7. 문서 내용 복사
        DocumentContent rejectedContent =
                documentContentRepository.findByDocument_Id(rejected.getId())
                        .orElseThrow(() -> new NotFoundException("문서 내용을 찾을 수 없습니다."));

        DocumentContent reviseContent =
                DocumentContent.revise(revised, rejectedContent);

        documentContentRepository.save(reviseContent);

        // 8. 결재선 초안 초기화
        approvalLineDomainService.initializeDraftLines(
                revised,
                rejectedTemplate,
                employee
        );

        // 9. 결과 반환
        return DocumentCreateResDto.from(revised.getId());    }
}
