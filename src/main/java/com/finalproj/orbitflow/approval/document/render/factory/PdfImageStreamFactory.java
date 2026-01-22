package com.finalproj.orbitflow.approval.document.render.factory;

import com.finalproj.orbitflow.approval.document.render.image.PdfInternalImageService;
import com.openhtmltopdf.extend.FSStream;
import com.openhtmltopdf.extend.FSStreamFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.Reader;

/**
 * PDF 렌더링 과정에서 사용하는 이미지 스트림을 제공하는 팩토리.
 * <p>
 * openhtmltopdf에서 요청하는 URL을 가로채어,
 * 내부에서 정의한 pdf-image 스킴을 실제 이미지 스트림으로 변환한다.
 * <p>
 * 문서 컴포넌트 이미지와 서명 이미지를 구분해 처리하며,
 * 실제 이미지 로딩은 PdfInternalImageService에 위임한다.
 * <p>
 * 이 클래스는 PDF 렌더링 시점에만 사용되며,
 * 외부에서 직접 이미지에 접근하는 용도로는 사용되지 않는다.
 *
 * @author : Choi MinHyeok
 * @filename : PdfImageStreamFactory
 * @since : 26. 1. 9. 금요일
 */


@Component
@RequiredArgsConstructor
public class PdfImageStreamFactory implements FSStreamFactory {

    private final PdfInternalImageService imageService;

    @Override
    public FSStream getUrl(String url) {

        if (url == null || !url.startsWith("pdf-image://")) {
            return null;
        }

        // pdf-image://{type}/{documentId}/{targetId} 형식
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