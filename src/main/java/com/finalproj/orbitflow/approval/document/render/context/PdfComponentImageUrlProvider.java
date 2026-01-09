package com.finalproj.orbitflow.approval.document.render.context;

import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : PdfComponentImageUrlProvider
 * @since : 26. 1. 4. 일요일
 **/


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
