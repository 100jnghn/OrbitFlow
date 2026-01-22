package com.finalproj.orbitflow.approval.document.render.context;

import org.springframework.stereotype.Component;

/**
 * PDF 렌더링 과정에서 사용하는 이미지 URL을 생성하는 구현체.
 * <p>
 * PDF 생성 시점에는 실제 HTTP URL 대신,
 * 내부 렌더링 로직에서 해석 가능한 전용 스킴 형태의 URL을 사용한다.
 * <p>
 * 이 구현체는 문서 ID와 파일 ID를 기반으로
 * PDF 컴포넌트 이미지용 식별자를 생성하는 역할만 담당한다.
 *
 * @author : Choi MinHyeok
 * @filename : PdfComponentImageUrlProvider
 * @since : 26. 1. 4. 일요일
 */


@Component
public class PdfComponentImageUrlProvider implements ImageUrlProvider {

    @Override
    public String generate(Long documentId, Long documentFileId) {
        return "pdf-image://component/"
                + documentId
                + "/"
                + documentFileId;
    }
}
