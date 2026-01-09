package com.finalproj.orbitflow.approval.document.render.factory;

import com.finalproj.orbitflow.approval.pdfInternalImage.service.PdfInternalImageService;
import com.openhtmltopdf.extend.FSStream;
import com.openhtmltopdf.extend.FSStreamFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.Reader;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : PdfImageStreamFactory
 * @since : 26. 1. 9. 금요일
 **/

@Component
@RequiredArgsConstructor
public class PdfImageStreamFactory implements FSStreamFactory {

    private final PdfInternalImageService imageService;

    @Override
    public FSStream getUrl(String url) {

        if (url == null || !url.startsWith("pdf-image://")) {
            return null;
        }

        // pdf-image://{type}/{documentId}/{targetId}
        String[] parts = url.replace("pdf-image://", "").split("/");

        if (parts.length < 3) {
            return null;
        }

        String type = parts[0];
        Long documentId = Long.valueOf(parts[1]);
        Long targetId = Long.valueOf(parts[2]);

        return switch (type) {

            case "component" -> createComponentImageStream(documentId, targetId);

            case "signature" -> createSignatureImageStream(documentId, targetId);

            default -> null;
        };
    }

    /* =========================
       Component Image
    ========================= */

    private FSStream createComponentImageStream(
            Long documentId,
            Long documentFileId
    ) {
        return new FSStream() {

            @Override
            public InputStream getStream() {
                try {
                    return imageService
                            .loadApprovedDocumentComponentImage(
                                    documentId,
                                    documentFileId
                            )
                            .resource()
                            .getInputStream();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            public Reader getReader() {
                return null;
            }
        };
    }

    /* =========================
       Signature Image
    ========================= */

    private FSStream createSignatureImageStream(
            Long documentId,
            Long approvalLineId
    ) {
        return new FSStream() {

            @Override
            public InputStream getStream() {
                try {
                    return imageService
                            .loadApprovedDocumentSignatureImage(
                                    documentId,
                                    approvalLineId
                            )
                            .resource()
                            .getInputStream();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            public Reader getReader() {
                return null;
            }
        };
    }
}