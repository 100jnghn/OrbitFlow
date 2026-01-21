package com.finalproj.orbitflow.approval.document.service.application;

import com.finalproj.orbitflow.approval.document.dto.PdfApprovalLineDto;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.render.factory.PdfImageStreamFactory;
import com.finalproj.orbitflow.approval.document.render.pdf.PdfHtmlBuilder;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.document.schema.FormTemplateSchemaParser;
import com.finalproj.orbitflow.approval.document.schema.PdfContentSchema;
import com.finalproj.orbitflow.approval.document.service.assembler.PdfApprovalLineAssembler;
import com.finalproj.orbitflow.approval.document.service.assembler.PdfContentSchemaAssembler;
import com.finalproj.orbitflow.approval.document.content.entity.DocumentContent;
import com.finalproj.orbitflow.approval.document.content.repository.DocumentContentRepository;
import com.finalproj.orbitflow.approval.document.file.service.DocumentFileService;
import com.finalproj.orbitflow.approval.form.template.schema.FormTemplateSchema;
import com.finalproj.orbitflow.approval.document.render.image.PdfInternalImageService;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.global.file.service.FileService;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 승인 완료된 결재 문서를 PDF로 변환하여 저장하는 Application Service.
 *
 * <p>
 * 이 서비스는 결재가 최종 승인(APPROVED)된 문서를 대상으로,
 * 문서 본문(JSON)과 승인선 정보를 기반으로 PDF를 생성하고
 * 생성된 PDF 파일을 시스템에 저장한 뒤 문서와 매핑한다.
 * </p>
 *
 * <p>
 * PDF 렌더링을 위한 HTML 생성, 내부 이미지 처리, 폰트 설정 등은
 * 전용 컴포넌트로 위임하며, 이 클래스는 전체 흐름만 조율한다.
 * </p>
 *
 * @author Choi MinHyeok
 * @since 2026.01.21
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentPdfApplicationService {

    private final DocumentRepository documentRepository;
    private final DocumentContentRepository documentContentRepository;

    private final PdfHtmlBuilder pdfHtmlBuilder;
    private final FormTemplateSchemaParser formTemplateSchemaParser;
    private final PdfContentSchemaAssembler pdfContentSchemaAssembler;
    private final PdfApprovalLineAssembler pdfApprovalLineAssembler;
    private final PdfInternalImageService pdfInternalImageService;

    private final FileService fileService;
    private final DocumentFileService documentFileService;

    @Value("${app.render-base-url}")
    private String renderBaseUrl;


    /**
     * 승인 완료된 문서를 PDF로 생성하고 저장한다.
     *
     * <p>
     * 이 메서드는 별도 트랜잭션(REQUIRES_NEW)에서 실행되며,
     * PDF 생성 중 오류가 발생하더라도 기존 승인 트랜잭션에는 영향을 주지 않는다.
     * </p>
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateAndStorePdf(Long documentId) {

        // 1. 문서 조회 및 상태 검증
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));

        if (document.getStatus() != DocumentStatus.APPROVED) {
            throw new IllegalStateException("승인 완료된 문서만 PDF로 생성할 수 있습니다.");
        }

        // 2. 문서 본문(JSON) 조회
        DocumentContent documentContent = documentContentRepository
                .findByDocument_Id(documentId)
                .orElseThrow(() ->
                        new NotFoundException("DocumentContent not found. documentId=" + documentId)
                );

        // 3. JSON → FormTemplateSchema 변환
        FormTemplateSchema templateSchema =
                formTemplateSchemaParser.parse(documentContent.getContentJson());

        if (templateSchema.getFields() == null || templateSchema.getFields().isEmpty()) {
            throw new IllegalStateException("양식에 필드가 없습니다.");
        }

        // 4. 승인선 정보 구성
        PdfApprovalLineDto approvalLine =
                pdfApprovalLineAssembler.from(documentId);

        // 5. PDF 전용 콘텐츠 스키마 구성
        PdfContentSchema pdfContentSchema =
                pdfContentSchemaAssembler.from(templateSchema);

        // 6. PDF 렌더링용 HTML 생성
        String html = pdfHtmlBuilder.build(
                documentId,
                approvalLine,
                pdfContentSchema,
                document.getWriter().getName(),
                document.getSubmittedAt()
        );

        // 7. HTML → PDF 변환
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();

            // 내부 이미지 스트림 처리
            builder.useProtocolsStreamImplementation(
                    new PdfImageStreamFactory(pdfInternalImageService),
                    "pdf-image"
            );

            // 폰트 설정
            registerFont(builder, "fonts/NanumGothic-Regular.ttf", "Nanum Gothic", 400);
            registerFont(builder, "fonts/NanumGothic-Bold.ttf", "Nanum Gothic", 700);

            builder.withHtmlContent(html, renderBaseUrl);
            builder.toStream(outputStream);
            builder.run();

            byte[] pdfBytes = outputStream.toByteArray();

            // 8. PDF 파일 저장 및 문서 매핑
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

    private void registerFont(
            PdfRendererBuilder builder,
            String classpath,
            String fontName,
            int weight
    ) {
        builder.useFont(
                () -> {
                    try {
                        return new ClassPathResource(classpath).getInputStream();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                fontName,
                weight,
                BaseRendererBuilder.FontStyle.NORMAL,
                true
        );
    }
}