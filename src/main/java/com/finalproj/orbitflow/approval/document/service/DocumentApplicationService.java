package com.finalproj.orbitflow.approval.document.service;

import com.finalproj.orbitflow.approval.approvalLine.entity.ApprovalLine;
import com.finalproj.orbitflow.approval.approvalLine.enums.ApprovalStatus;
import com.finalproj.orbitflow.approval.approvalLine.repository.ApprovalLineRepository;
import com.finalproj.orbitflow.approval.approvalLine.service.ApprovalLineDomainService;
import com.finalproj.orbitflow.approval.document.dto.DocumentCreateResDto;
import com.finalproj.orbitflow.approval.document.dto.PdfApprovalLineDto;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.render.pdf.PdfHtmlBuilder;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.document.schema.PdfContentSchema;
import com.finalproj.orbitflow.approval.documentContent.entity.DocumentContent;
import com.finalproj.orbitflow.approval.documentContent.repository.DocumentContentRepository;
import com.finalproj.orbitflow.approval.documentFile.entity.DocumentFile;
import com.finalproj.orbitflow.approval.documentFile.enums.DocumentFileStatus;
import com.finalproj.orbitflow.approval.documentFile.repository.DocumentFileRepository;
import com.finalproj.orbitflow.approval.documentSignature.service.DocumentSignatureService;
import com.finalproj.orbitflow.approval.formTemplate.entity.FormTemplate;
import com.finalproj.orbitflow.approval.formTemplate.repository.FormTemplateRepository;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;
import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.global.file.repository.FileRepository;
import com.finalproj.orbitflow.global.file.service.FileService;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;


/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentApplicationService
 * @since : 25. 12. 28. 일요일
 **/

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DocumentApplicationService {

    private final DocumentRepository documentRepository;
    private final DocumentContentRepository documentContentRepository;
    private final FormTemplateRepository formTemplateRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final ApprovalLineDomainService approvalLineDomainService;
    private final ObjectMapper objectMapper;
    private final ApprovalLineRepository approvalLineRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DocumentFileRepository documentFileRepository;
    private final FileRepository fileRepository;
    private final FileService fileService;
    private final PdfHtmlBuilder pdfHtmlBuilder;
    private final PdfContentSchemaAssembler pdfContentSchemaAssembler;
    private final PdfApprovalLineAssembler pdfApprovalLineAssembler;
    private final DocumentSignatureService documentSignatureService;

    @Value("${app.base-url}")
    private String baseUrl;


    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public DocumentCreateResDto createDraft(
            Long companyId,
            Long employeeId,
            Long formTemplateId,
            Long beforeDocumentId
    ) {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new NotFoundException("회사를 찾지 못했습니다."));

        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new NotFoundException("사원을 찾지 못했습니다."));

        FormTemplate formTemplate = formTemplateRepository.findByIdAndCompany_id(formTemplateId, companyId).orElseThrow(() -> new InvalidRequestException("사용할 수 없는 양식입니다."));

        if (!formTemplate.getTemplateGroup().getActive()) {
            throw new InvalidRequestException("해당 양식은 현재 사용 불가 상태입니다.");
        }

        // 1. 활성화 여부
        if (!formTemplate.isActive()) {
            throw new InvalidRequestException("활성화되지 않은 양식입니다.");
        }

        // 2. 결재 규칙 존재 여부
        if (formTemplate.getApprovalRuleJson() == null
                || formTemplate.getApprovalRuleJson().isBlank()) {
            throw new InvalidRequestException("결재 규칙이 설정되지 않은 양식입니다.");
        }

        // 3. 결재 규칙 사전 검증 (강력 추천)
        try {
            approvalLineDomainService.validateApprovalRule(formTemplate);
        } catch (Exception e) {
            throw new InvalidRequestException(
                    "결재 규칙이 올바르지 않은 양식입니다." + e
            );
        }

        FormTemplateSchema schema = parseSchema(formTemplate.getTemplateJson());

        if (schema.getFields() == null || schema.getFields().isEmpty()) {
            throw new IllegalStateException("양식에 필드가 없습니다.");
        }

        String baseTitle = extractDefaultTitle(schema);
        String finalTitle = baseTitle + " (" + LocalDate.now() + ")";

        Document beforeDocument = null;
        if (beforeDocumentId != null) {
            beforeDocument = getDocumentForWriter(employee, beforeDocumentId);
        }

        Document createdDocument = Document.createDraft(
                company,
                employee,
                formTemplate,
                finalTitle,
                beforeDocument
        );

        documentRepository.save(createdDocument);


        DocumentContent documentContent = DocumentContent.fromSchema(createdDocument, schema, objectMapper);

        documentContentRepository.save(documentContent);

        approvalLineDomainService.initializeDraftLines(
                createdDocument,
                formTemplate,
                employee
        );

        return DocumentCreateResDto.from(createdDocument.getId());
    }

    @Transactional
    public void approve(Long employeeId, Long documentId, String comment) {

        // 1. 문서 조회 + 상태 검증
        Document document = getDocumentForApprover(documentId);

        if (document.getStatus() != DocumentStatus.IN_PROGRESS) {
            throw new InvalidRequestException("진행중인 결재 문서만 승인할 수 있습니다.");
        }

        // 2. 결재선 조회
        List<ApprovalLine> lines = getApprovalLines(documentId);


        // 3. 내 결재선 찾기
        ApprovalLine myLine = lines.stream()
                .filter(line ->
                        line.getApprover().getId().equals(employeeId)
                                && line.getStatus() == ApprovalStatus.IN_PROGRESS
                )
                .findFirst()
                .orElseThrow(() ->
                        new InvalidRequestException("자신의 결재 차례가 아닙니다.")
                );


        if (myLine.getStatus() != ApprovalStatus.IN_PROGRESS) {
            throw new InvalidRequestException("자신의 결재 차례에만 승인할 수 있습니다.");
        }

        // 4. 내 결재선 승인
        myLine.markApproved(comment); // → APPROVED

        documentSignatureService.snapShotDocumentSignature(document, myLine);

        // 5. 다음 결재선 찾기
        ApprovalLine nextLine = lines.stream()
                .filter(line ->
                        line.getOrderNo() > myLine.getOrderNo()
                                && line.getStatus() == ApprovalStatus.WAITING
                )
                .findFirst()
                .orElse(null);

        if (nextLine != null) {
            // 6-1. 다음 결재자가 있다 → 결재 계속
            nextLine.markInProgress(); // WAITING → IN_PROGRESS
        } else {
            // 6-2. 다음 결재자가 없다 → 최종 승인
            document.approve();
            applicationEventPublisher.publishEvent(document.getId());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateAndStorePdf(Long documentId) {

        // 1️⃣ 문서 조회
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));

        if (document.getStatus() != DocumentStatus.APPROVED) {
            throw new IllegalStateException("승인 완료된 문서만 PDF로 생성할 수 있습니다.");
        }

        // 2️⃣ 문서 본문(JSON)
        DocumentContent content = documentContentRepository
                .findByDocument_Id(documentId)
                .orElseThrow(() -> new NotFoundException(
                        "DocumentContent not found. documentId=" + documentId
                ));

        // 3️⃣ JSON → FormTemplateSchema
        FormTemplateSchema schema = parseSchema(content.getContentJson());
        if (schema.getFields() == null || schema.getFields().isEmpty()) {
            throw new IllegalStateException("양식에 필드가 없습니다.");
        }

        // 4️⃣ 승인선
        PdfApprovalLineDto approvalLine =
                pdfApprovalLineAssembler.from(documentId);

        // 5️⃣ FormTemplateSchema → PdfContentSchema
        PdfContentSchema pdfSchema =
                pdfContentSchemaAssembler.from(schema);

        // 6️⃣ HTML 생성
        String html = pdfHtmlBuilder.build(
                documentId,
                approvalLine,
                pdfSchema,
                document.getWriter().getName(),
                document.getSubmittedAt()
        );

        log.info("===== PDF HTML START =====");
        log.info(html);
        log.info("===== PDF HTML END =====");

        // 7️⃣ HTML → PDF
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            PdfRendererBuilder builder = new PdfRendererBuilder();

            // 🔥 성능 + 안정 모드
            builder.useFastMode();

            // 🔥 폰트 먼저 등록 (시스템 폰트 스캔 차단)
            builder.useFont(
                    () -> {
                        try {
                            return new ClassPathResource("fonts/NanumGothic-Regular.ttf")
                                    .getInputStream();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    "Nanum Gothic",
                    400,
                    FontStyle.NORMAL,
                    true
            );

            builder.useFont(
                    () -> {
                        try {
                            return new ClassPathResource("fonts/NanumGothic-Bold.ttf")
                                    .getInputStream();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    "Nanum Gothic",
                    700,
                    FontStyle.NORMAL,
                    true
            );

      /*      String baseUri = requireNonNull(
                    getClass().getClassLoader().getResource("static/")
            ).toExternalForm();*/

            builder.withHtmlContent(html, baseUrl);

            builder.toStream(os);
            builder.run();

            byte[] pdfBytes = os.toByteArray();

            // 8️⃣ PDF 저장
            fileService.saveGeneratedPdf(
                    document.getCompany().getId(),
                    documentId,
                    pdfBytes
            );

        } catch (IOException e) {
            throw new RuntimeException("PDF 생성 실패", e);
        }
    }

    @Transactional
    public void reject(Long employeeId, Long documentId, String comment) {

        // 1. 문서 조회 + 상태 검증
        Document document = getDocumentForApprover(documentId);

        if (document.getStatus() != DocumentStatus.IN_PROGRESS) {
            throw new InvalidRequestException("진행중인 결재 문서만 반려할 수 있습니다.");
        }

        // 2. 결재선 조회
        List<ApprovalLine> lines = getApprovalLines(documentId);


        // 3. 내 결재선 찾기
        ApprovalLine myLine = lines.stream()
                .filter(line ->
                        line.getApprover().getId().equals(employeeId)
                                && line.getStatus() == ApprovalStatus.IN_PROGRESS
                )
                .findFirst()
                .orElseThrow(() ->
                        new InvalidRequestException("자신의 결재 차례가 아닙니다.")
                );


        if (myLine.getStatus() != ApprovalStatus.IN_PROGRESS) {
            throw new InvalidRequestException("자신의 결재 차례에만 반려할 수 있습니다.");
        }

        // 4. 내 결재선 반려
        myLine.reject(comment); // → REJECTED

        // 5. 내 이후 결재자 CANCELLED 처리
        lines.stream()
                .filter(line ->
                        line.getOrderNo() > myLine.getOrderNo()
                                && line.getStatus() == ApprovalStatus.WAITING
                )
                .forEach(ApprovalLine::markCancelled); // → CANCELLED

        // 6. 문서 상태 반려
        document.reject();
    }

    @Transactional
    public DocumentCreateResDto reviseDocument(Long employeeId, Long documentId) {

        // 1. 작성자 조회
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("사원을 찾지 못했습니다."));

        // 2. 원본 문서 조회 (작성자 검증 포함)
        Document rejected = getDocumentForWriter(employee, documentId);

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
        return DocumentCreateResDto.from(revised.getId());
    }

    @Transactional
    public void submitDocument(Long employeeId, Long documentId) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("요청한 사원 정보를 찾을 수 없습니다."));

        Document document = getDocumentForWriter(employee, documentId);

        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new InvalidRequestException(
                    "이미 상신되었거나 처리 중인 문서는 다시 상신할 수 없습니다."
            );
        }

        // 1. 문서 상태 변경
        document.submit();

        // 2. 결재선 처리
        List<ApprovalLine> lines =
                approvalLineRepository.findByDocument_IdOrderByOrderNoAsc(documentId);

        if (lines.isEmpty()) {
            throw new InvalidRequestException(
                    "결재선이 지정되지 않은 문서는 상신할 수 없습니다."
            );
        }

        lines.forEach(ApprovalLine::markWaiting);
        lines.get(0).markInProgress();

        // 3. 첨부파일 조회
        List<DocumentFile> documentFiles =
                documentFileRepository.findByDocument_Id(documentId);

        // 4. TEMP → FINAL
        documentFiles.stream()
                .filter(df -> df.getStatus() == DocumentFileStatus.TEMP)
                .forEach(df -> df.updateStatus(DocumentFileStatus.FINAL));

        // 5. DELETED 파일 분리
        List<DocumentFile> deletedFiles = documentFiles.stream()
                .filter(df -> df.getStatus() == DocumentFileStatus.DELETED)
                .toList();

        // 6. 문서-파일 관계 먼저 제거
        documentFileRepository.deleteAll(deletedFiles);

        // DB 반영
        entityManager.flush();

        // 7. File 참조 수 체크 후 실제 삭제
        for (DocumentFile df : deletedFiles) {
            File file = df.getFile();

            long refCount =
                    documentFileRepository.countByFile_Id(file.getId());

            if (refCount == 0) {
                fileRepository.delete(file);
                fileService.deleteObjectAfterCommit(file.getObjectKey());
            }
        }
    }

    private List<ApprovalLine> getApprovalLines(Long documentId) {
        List<ApprovalLine> lines =
                approvalLineRepository.findByDocument_IdOrderByOrderNoAsc(documentId);

        if (lines.isEmpty()) {
            throw new NotFoundException("문서의 결재선을 찾을 수 없습니다.");
        }
        return lines;
    }

    private FormTemplateSchema parseSchema(String templateJson) {
        try {
            return objectMapper.readValue(templateJson, FormTemplateSchema.class);
        } catch (Exception e) {
            throw new IllegalStateException("template_json 파싱 실패", e);
        }
    }

    private String extractDefaultTitle(FormTemplateSchema schema) {

        return schema.getFields().stream()
                .filter(f -> "document-title".equals(f.getFieldId()))
                .findFirst()
                .map(f -> {
                    Object value = f.getMeta().get("value");
                    if (value == null || value.toString().isBlank()) {
                        return "제목 없음"; // fallback
                    }
                    return value.toString();
                })
                .orElse("제목 없음");
    }

    private Document getDocumentForWriter(Employee employee, Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾지 못했습니다."));

        if (!document.getWriter().equals(employee)) {
            throw new ForbiddenException("작성자만 접근할 수 있습니다.");
        }
        return document;
    }

    private Document getDocumentForApprover(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾지 못했습니다."));
    }
}
