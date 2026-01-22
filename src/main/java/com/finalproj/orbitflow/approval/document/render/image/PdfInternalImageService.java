package com.finalproj.orbitflow.approval.document.render.image;

import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.file.entity.DocumentFile;
import com.finalproj.orbitflow.approval.document.file.repository.DocumentFileRepository;
import com.finalproj.orbitflow.approval.document.file.service.DocumentFileService;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.document.signature.entity.DocumentSignature;
import com.finalproj.orbitflow.approval.document.signature.repository.DocumentSignatureRepository;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;


/**
 * PDF 렌더링 과정에서 내부 이미지 리소스를 조회하는 전용 서비스.
 * <p>
 * PDF 생성 시 HTML 내부에서 참조되는 이미지 요청을 처리하며,
 * 일반적인 HTTP 요청이 아닌 내부 렌더링 흐름에서만 사용된다.
 * <p>
 * 승인 완료된 문서(DocumentStatus.APPROVED)에 대해서만
 * 이미지 접근을 허용하며,
 * 문서와 파일 간의 관계를 검증한 뒤 실제 이미지 리소스를 반환한다.
 * <p>
 * 처리 대상은 다음 두 가지로 구분된다.
 * - 문서 컴포넌트에 첨부된 이미지
 * - 결재 단계에서 사용된 서명 이미지
 * <p>
 * 이 서비스는 권한 검증, 상태 검증, 리소스 로딩만을 담당하며,
 * 이미지 URL 해석이나 스트림 변환 로직은 상위 렌더링 계층에서 처리한다.
 *
 * @author : Choi MinHyeok
 * @filename : PdfInternalImageService
 * @since : 26. 1. 4. 일요일
 */


@Service
@RequiredArgsConstructor
public class PdfInternalImageService {

    private final DocumentRepository documentRepository;
    private final DocumentFileRepository documentFileRepository;
    private final DocumentSignatureRepository documentSignatureRepository;

    private final DocumentFileService documentFileService;
    private final FileService fileService;

    public PdfImageResource loadApprovedDocumentComponentImage(
            Long documentId,
            Long documentFileId
    ) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new InvalidRequestException("Document not found"));

        if (document.getStatus() != DocumentStatus.APPROVED) {
            throw new InvalidRequestException("PDF 이미지 접근 불가");
        }


        DocumentFile documentFile = documentFileRepository.findById(documentFileId)
                        .orElseThrow(() -> new InvalidRequestException("Document file not found"));

        if (!documentFile.getDocument().getId().equals(documentId)) {
            throw new InvalidRequestException("문서-파일 불일치");
        }

        Resource resource =
                documentFileService.loadImage(documentFileId);

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(documentFile.getFile().getContentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }


        return new PdfImageResource(resource, mediaType);
    }


    public PdfImageResource loadApprovedDocumentSignatureImage(
            Long documentId,
            Long approvalLineId
    ) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new InvalidRequestException("Document not found"));

        if (document.getStatus() != DocumentStatus.APPROVED) {
            throw new InvalidRequestException("서명 이미지 접근 불가");
        }

        DocumentSignature documentSignature = documentSignatureRepository.findByApprovalLine_Id(approvalLineId)
                .orElseThrow(() -> new InvalidRequestException("Signature not found"));


        if (!documentSignature.getDocument().getId().equals(documentId)) {
            throw new InvalidRequestException("문서-결재자 불일치");
        }

        Resource resource = fileService.loadAsResource(documentSignature.getSignatureFile());

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(documentSignature.getSignatureFile().getContentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }


        return new PdfImageResource(resource, mediaType);
    }
}
