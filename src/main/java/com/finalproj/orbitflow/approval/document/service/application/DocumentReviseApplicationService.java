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
 * 반려된 결재 문서를 기준으로 재기안(DRAFT) 문서를 생성하는 Application Service.
 * <p>
 * 이 클래스는 결재가 반려(REJECTED)된 문서에 대해,
 * 기존 문서를 직접 수정하는 대신 새로운 초안 문서를 생성하는
 * 재기안 유즈케이스를 담당한다.
 * <p>
 * 재기안 과정에서는 다음 작업들이 수행된다.
 * <p>
 * - 재기안 요청자(작성자) 검증
 * - 반려 상태 문서 여부 및 중복 재기안 여부 검증
 * - 반려 시점에 사용된 양식 버전 조회
 * - 기존 문서를 기반으로 한 새로운 DRAFT 문서 생성
 * - 문서 본문(JSON) 복사
 * - 첨부 파일(DocumentFile) 관계 복제
 * - 결재선 초안 재초기화
 * <p>
 * 원본 문서와 재기안 문서는 beforeDocument 관계로 연결되며,
 * 이력을 보존한 상태에서 수정 작업을 이어갈 수 있도록 설계되었다.
 * <p>
 * 이 클래스는 재기안 흐름에 필요한 조합 로직만을 담당하며,
 * 개별 검증이나 도메인 규칙은 각 전용 컴포넌트로 위임한다.
 *
 * @author : Choi MinHyeok
 * @filename : DocumentReviseApplicationService
 * @since : 26. 1. 21. 수요일
 */


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
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("사원을 찾지 못했습니다."));

        Document rejected = documentAccessValidator.getForWriter(employee, documentId);

        if (rejected.getStatus() != DocumentStatus.REJECTED) {
            throw new InvalidRequestException("반려 상태의 문서만 재기안할 수 있습니다.");
        }

        boolean existsRevision =
                documentRepository.existsByBeforeDocument_Id(rejected.getId());

        if (existsRevision) {
            throw new InvalidRequestException("이미 재기안 문서가 존재합니다.");
        }

        FormTemplate rejectedTemplate =
                formTemplateRepository
                        .findByTemplateGroup_idAndVersion(
                                rejected.getTemplateGroup().getId(),
                                rejected.getTemplateVersion()
                        )
                        .orElseThrow(() -> new NotFoundException("문서 양식 조회 실패"));

        Document revised = Document.reviseDraft(rejected);
        documentRepository.save(revised);

        List<DocumentFile> originalFiles =
                documentFileRepository.findByDocument_Id(rejected.getId());

        for (DocumentFile df : originalFiles) {
            if (df.getStatus() == DocumentFileStatus.DELETED) continue;

            DocumentFile copied =
                    DocumentFile.copyFor(revised, df);

            documentFileRepository.save(copied);
        }

        DocumentContent rejectedContent =
                documentContentRepository.findByDocument_Id(rejected.getId())
                        .orElseThrow(() -> new NotFoundException("문서 내용을 찾을 수 없습니다."));

        DocumentContent reviseContent =
                DocumentContent.revise(revised, rejectedContent);

        documentContentRepository.save(reviseContent);

        approvalLineDomainService.initializeDraftLines(
                revised,
                rejectedTemplate,
                employee
        );

        return DocumentCreateResDto.from(revised.getId());
    }
}
