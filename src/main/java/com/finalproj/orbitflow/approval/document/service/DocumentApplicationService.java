package com.finalproj.orbitflow.approval.document.service;

import com.finalproj.orbitflow.approval.approvalLine.entity.ApprovalLine;
import com.finalproj.orbitflow.approval.approvalLine.enums.ApprovalStatus;
import com.finalproj.orbitflow.approval.approvalLine.repository.ApprovalLineRepository;
import com.finalproj.orbitflow.approval.approvalLine.service.ApprovalLineDomainService;
import com.finalproj.orbitflow.approval.attendanceRecord.entity.AttendanceRecord;
import com.finalproj.orbitflow.approval.attendanceRecord.repository.AttendanceRecordRepository;
import com.finalproj.orbitflow.approval.document.dto.DocumentCreateResDto;
import com.finalproj.orbitflow.approval.document.dto.DocumentMainInfoResDto;
import com.finalproj.orbitflow.approval.document.dto.LeaveCalculationResult;
import com.finalproj.orbitflow.approval.document.dto.PdfApprovalLineDto;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.render.factory.PdfImageStreamFactory;
import com.finalproj.orbitflow.approval.document.render.pdf.PdfHtmlBuilder;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.document.schema.PdfContentSchema;
import com.finalproj.orbitflow.approval.documentContent.entity.DocumentContent;
import com.finalproj.orbitflow.approval.documentContent.repository.DocumentContentRepository;
import com.finalproj.orbitflow.approval.documentFile.entity.DocumentFile;
import com.finalproj.orbitflow.approval.documentFile.enums.DocumentFileStatus;
import com.finalproj.orbitflow.approval.documentFile.repository.DocumentFileRepository;
import com.finalproj.orbitflow.approval.documentFile.service.DocumentFileService;
import com.finalproj.orbitflow.approval.documentSignature.service.DocumentSignatureService;
import com.finalproj.orbitflow.approval.formTemplate.entity.FormTemplate;
import com.finalproj.orbitflow.approval.formTemplate.repository.FormTemplateRepository;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;
import com.finalproj.orbitflow.approval.formTemplateGroup.enums.BaseRole;
import com.finalproj.orbitflow.approval.pdfInternalImage.service.PdfInternalImageService;
import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.global.file.repository.FileRepository;
import com.finalproj.orbitflow.global.file.service.FileService;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.notification.enums.NotificationType;
import com.finalproj.orbitflow.notification.service.NotificationCommandService;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

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
    private final PdfInternalImageService pdfInternalImageService;
    private final DocumentSignatureService documentSignatureService;
    private final DocumentFileService documentFileService;
    private final NotificationCommandService  notificationCommandService;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final LeaveCalculationService leaveCalculationService;

    @Value("${app.render-base-url}")
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

        // (중복이지만 기존 코드 유지)
        if (myLine.getStatus() != ApprovalStatus.IN_PROGRESS) {
            throw new InvalidRequestException("자신의 결재 차례에만 승인할 수 있습니다.");
        }

        // 4. 내 결재선 승인
        myLine.markApproved(comment);

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

            // 결재자 상태 검증 로직

            Employee nextApprover = nextLine.getApprover();

            // 6-1. 다음 결재자 유효성 검증
            if (!isValidApprover(nextApprover)
                    || !matchesLineRole(nextLine, nextApprover)) {

                // 조직/직책 기준으로 대체 결재자 탐색
                Employee replacement = findReplacementApprover(nextLine);

                if (replacement == null) {
                    // 대체 결재자 없음 → 시스템 반려
                    rejectBySystem(
                            document,
                            lines,
                            myLine,
                            "결재자 없음"
                    );
                    return;
                }

                // 대체 결재자 지정
                nextLine.setApprover(replacement);
            }

            // 6-2. 다음 결재 진행
            nextLine.markInProgress();

            String shortTitle = shortenTitle(document.getTitle(), 20);

            String content =
                    "[결재 요청 - " + formatNow() + "]\n" +
                            "문서 제목 : " + shortTitle + "\n" +
                            "기안자 : " + document.getWriter().getName() +
                            " | " + document.getWriter().getOrganization().getName() +
                            " | " + document.getWriter().getPositionCategory().getName();

            notificationCommandService.createNotification(
                    nextLine.getCompany().getId(),
                    nextLine.getApprover().getId(),
                    NotificationType.APPROVAL,
                    content,
                    "/view/document/" + documentId
            );

        } else {
            // 6-3. 다음 결재자가 없다 → 최종 승인 (기존 로직 그대로)
            document.approve();

            String shortTitle = shortenTitle(document.getTitle(), 20);

            String content =
                    "[결재 완료 - " + formatNow() + "]\n" +
                            "문서 제목 : " + shortTitle + "\n" +
                            "최종 승인자 : " + myLine.getApprover().getName() +
                            " | " + myLine.getApprover().getOrganization().getName() +
                            " | " + myLine.getApprover().getPositionCategory().getName();

            notificationCommandService.createNotification(
                    document.getCompany().getId(),
                    document.getWriter().getId(),
                    NotificationType.APPROVAL,
                    content,
                    "/view/document/" + documentId
            );

            applicationEventPublisher.publishEvent(document.getId());
        }
    }

    @Transactional
    public void reject(Long employeeId, Long documentId, String comment) {

        Document document = getDocumentForApprover(documentId);

        if (document.getStatus() != DocumentStatus.IN_PROGRESS) {
            throw new InvalidRequestException("진행중인 결재 문서만 반려할 수 있습니다.");
        }

        List<ApprovalLine> lines = getApprovalLines(documentId);

        ApprovalLine myLine = lines.stream()
                .filter(line ->
                        line.getApprover().getId().equals(employeeId)
                                && line.getStatus() == ApprovalStatus.IN_PROGRESS
                )
                .findFirst()
                .orElseThrow(() ->
                        new InvalidRequestException("자신의 결재 차례가 아닙니다.")
                );

        rejectInternal(document, lines, myLine, comment, false);
    }

    private void rejectInternal(
            Document document,
            List<ApprovalLine> lines,
            ApprovalLine rejectLine,
            String comment,
            boolean systemReject
    ) {
        // 4. 내 결재선 반려
        rejectLine.reject(comment); // → REJECTED

        String shortTitle = shortenTitle(document.getTitle(), 20);

        String header = systemReject ? "자동 반려" : "결재 문서 반려";

        String content =
                "[" + header + " - " + formatNow() + "]\n" +
                        "문서 제목 : " + shortTitle + "\n" +
                        (systemReject
                                ? "사유 : " + comment
                                : "반려자 : " + rejectLine.getApprover().getName()
                                + " | " + rejectLine.getApprover().getOrganization().getName()
                                + " | " + rejectLine.getApprover().getPositionCategory().getName()
                        );


        notificationCommandService.createNotification(
                document.getCompany().getId(),
                document.getWriter().getId(),
                NotificationType.APPROVAL,
                content,
                "/view/document/" + document.getId()
        );

        // 5. 내 이후 결재자 CANCELLED 처리
        lines.stream()
                .filter(line ->
                        line.getOrderNo() > rejectLine.getOrderNo()
                                && line.getStatus() == ApprovalStatus.WAITING
                )
                .forEach(ApprovalLine::markCancelled); // → CANCELLED

        // 6. 문서 상태 반려
        document.reject();

        // 근태 반영 롤백
        attendanceRecordRepository.findBySourceDocument_Id(document.getId())
                .ifPresent(AttendanceRecord::rejectedDocument);
    }

    private void rejectBySystem(
            Document document,
            List<ApprovalLine> lines,
            ApprovalLine actorLine,
            String reason
    ) {
        // actorLine: 현재 IN_PROGRESS 또는 방금 승인한 라인
        // (시스템 사용자 엔티티가 없다면, 가장 자연스럽게 로그/히스토리를 남길 수 있는 라인을 사용)
        rejectInternal(document, lines, actorLine, reason, true);
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

        // 2. 결재선 조회
        List<ApprovalLine> lines =
                approvalLineRepository.findByDocument_IdOrderByOrderNoAsc(documentId);

        if (lines.isEmpty()) {
            throw new InvalidRequestException(
                    "결재선이 지정되지 않은 문서는 상신할 수 없습니다."
            );
        }

        // 🔽🔽🔽 추가된 최초 결재자 검증/대체 로직 🔽🔽🔽

        ApprovalLine firstLine = lines.get(0);
        Employee firstApprover = firstLine.getApprover();

        if (!isValidApprover(firstApprover)
                || !matchesLineRole(firstLine, firstApprover)) {

            Employee replacement = findReplacementApprover(firstLine);

            if (replacement == null) {
                // 최초 결재자조차 없음 → 상신 자체 불가
                throw new InvalidRequestException(
                        "현재 결재 규칙에 해당하는 결재자가 없어 문서를 상신할 수 없습니다."
                );
            }

            firstLine.setApprover(replacement);
        }

        // 3. 결재선 상태 변경
        lines.forEach(ApprovalLine::markWaiting);
        firstLine.markInProgress();

        // 4. 첨부파일 조회
        List<DocumentFile> documentFiles =
                documentFileRepository.findByDocument_Id(documentId);

        // 5. TEMP → FINAL
        documentFiles.stream()
                .filter(df -> df.getStatus() == DocumentFileStatus.TEMP)
                .forEach(df -> df.updateStatus(DocumentFileStatus.FINAL));

        // 6. DELETED 파일 분리
        List<DocumentFile> deletedFiles = documentFiles.stream()
                .filter(df -> df.getStatus() == DocumentFileStatus.DELETED)
                .toList();

        // 7. 문서-파일 관계 제거
        documentFileRepository.deleteAll(deletedFiles);

        entityManager.flush();

        // 8. 실제 파일 삭제
        cleanupDetachedFiles(deletedFiles);


        // 9. 휴가 문서 처리 (기존 로직 그대로)
        BaseRole baseRole = document.getTemplateGroup().getBaseRole();

        if (BaseRole.VACATION.equals(baseRole)) {

            LeaveCalculationResult result =
                    leaveCalculationService.calculate(document);

            LocalDate actualStart = result.effectiveDates().get(0);
            LocalDate actualEnd = result.effectiveDates()
                    .get(result.effectiveDates().size() - 1);

            AttendanceRecord record = AttendanceRecord.builder()
                    .employee(document.getWriter())
                    .company(document.getCompany())
                    .startDate(actualStart)
                    .endDate(actualEnd)
                    .days(result.days())
                    .leaveType(result.leaveType())
                    .reason(result.payload().reason())
                    .sourceDocument(document)
                    .status(DocumentStatus.IN_PROGRESS)
                    .approvedAt(null)
                    .build();

            attendanceRecordRepository.save(record);
        }

        // 10. 최초 결재자 알림
        String shortTitle = shortenTitle(document.getTitle(), 20);

        String content =
                "[결재 요청 - " + formatNow() + "]\n" +
                        "문서 제목 : " + shortTitle + "\n" +
                        "기안자 : " + document.getWriter().getName() +
                        " | " + document.getWriter().getOrganization().getName() +
                        " | " + document.getWriter().getPositionCategory().getName();

        notificationCommandService.createNotification(
                firstLine.getCompany().getId(),
                firstLine.getApprover().getId(),
                NotificationType.APPROVAL,
                content,
                "/view/document/" + documentId
        );
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
                .orElseThrow(() ->
                        new NotFoundException("DocumentContent not found. documentId=" + documentId)
                );

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

        /* =========================
           기본 설정
        ========================= */
            builder.useFastMode();

        /* =========================
           🔥 pdf-image:// 스트림 팩토리 등록 (핵심)
        ========================= */
            builder.useProtocolsStreamImplementation(
                    new PdfImageStreamFactory(pdfInternalImageService),
                    "pdf-image"
            );

        /* =========================
           🔥 폰트 등록
        ========================= */
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

        /* =========================
           HTML → PDF
        ========================= */
            builder.withHtmlContent(html, baseUrl);
            builder.toStream(os);
            builder.run();

            byte[] pdfBytes = os.toByteArray();

            // 8️⃣ PDF 저장
            File pdfFile = fileService.saveGeneratedPdf(
                    document.getCompany().getId(),
                    documentId,
                    pdfBytes
            );

            documentFileService.mappingPdf(document, pdfFile);

        } catch (Exception e) {
            throw new RuntimeException("PDF 생성 실패", e);
        }
    }

    private boolean isValidApprover(Employee employee) {
        return employee != null
                && employee.getStatus() == EmployeeStatus.ACTIVE;
    }

    private boolean matchesLineRole(ApprovalLine line, Employee employee) {
        return employee != null
                && Objects.equals(
                employee.getOrganization().getId(),
                line.getOrganization().getId()
        )
                && Objects.equals(
                employee.getPositionCategory().getId(),
                line.getPositionCategory().getId()
        );
    }

    private Employee findReplacementApprover(ApprovalLine line) {
        if (line.getOrganization() == null || line.getPositionCategory() == null) {
            return null;
        }

        return employeeRepository
                .findHeadByOrgIdAndPositionCategoryIdAndStatus(
                        line.getOrganization().getId(),
                        line.getPositionCategory().getId(),
                        EmployeeStatus.ACTIVE
                )
                .orElse(null);
    }

    private String shortenTitle(String title, int maxLength) {
        if (title == null) return "";
        if (title.length() <= maxLength) {
            return title;
        }
        return title.substring(0, maxLength) + "...";
    }

    private String formatNow() {
        return java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
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

    public DocumentMainInfoResDto getMainInfo(Long employeeId) {

        int waitingCount =
                approvalLineRepository.countByApproverAndStatus(
                        employeeId,
                        ApprovalStatus.WAITING
                );

        int progressCount =
                documentRepository.countByWriterAndStatus(
                        employeeId,
                        DocumentStatus.IN_PROGRESS
                );

        int rejectCount =
                documentRepository.countRejectedNotResubmitted(
                        employeeId,
                        DocumentStatus.REJECTED
                );

        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime now = ZonedDateTime.now(zone);

        Instant startOfThisMonth =
                now.withDayOfMonth(1)
                        .toLocalDate()
                        .atStartOfDay(zone)
                        .toInstant();

        Instant startOfLastMonth =
                now.minusMonths(1)
                        .withDayOfMonth(1)
                        .toLocalDate()
                        .atStartOfDay(zone)
                        .toInstant();

        int monthApprovedCount =
                documentRepository.countByWriterAndStatusFromDate(
                        employeeId,
                        DocumentStatus.APPROVED,
                        startOfThisMonth
                );

        int beforeMonthApprovedCount =
                documentRepository.countByWriterAndStatusBetween(
                        employeeId,
                        DocumentStatus.APPROVED,
                        startOfLastMonth,
                        startOfThisMonth
                );

        return new DocumentMainInfoResDto(
                waitingCount,
                progressCount,
                rejectCount,
                monthApprovedCount,
                beforeMonthApprovedCount
        );
    }

    @Transactional
    public void deleteDraftDocument(Long employeeId, Long documentId) {

        // 1️⃣ 문서 조회 + 기본 검증
        Document draft = validateDeletableDraft(employeeId, documentId);

        // 2️⃣ 첨부파일 관계 삭제
        List<DocumentFile> documentFiles =
                documentFileRepository.findByDocument_Id(draft.getId());

        documentFileRepository.deleteAll(documentFiles);
        entityManager.flush();

        cleanupDetachedFiles(documentFiles);

        // 3️⃣ 결재선 삭제 (DRAFT 전용)
        approvalLineRepository.deleteByDocument(draft);

        // 4️⃣ 문서 본문 삭제
        documentContentRepository.deleteByDocument(draft);

        // 5️⃣ 문서 삭제
        documentRepository.delete(draft);
    }

    private Document validateDeletableDraft(Long employeeId, Long documentId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서 탐색 실패"));

        // 상태 검증
        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new InvalidRequestException("임시 문서만 삭제할 수 있습니다.");
        }

        // 작성자 검증
        if (!document.getWriter().getId().equals(employeeId)) {
            throw new InvalidRequestException("삭제 권한 없음");
        }

        return document;
    }

    private void cleanupDetachedFiles(List<DocumentFile> detachedFiles) {

        for (DocumentFile df : detachedFiles) {

            File file = df.getFile();
            if (file == null) {
                continue; // 참조 문서 / URL 타입
            }

            String objectKey = file.getObjectKey();

            long refCount =
                    documentFileRepository.countByFile_Id(file.getId());

            if (refCount == 0) {
                fileRepository.delete(file);
                fileService.deleteObjectAfterCommit(objectKey);
            }
        }
    }

}
