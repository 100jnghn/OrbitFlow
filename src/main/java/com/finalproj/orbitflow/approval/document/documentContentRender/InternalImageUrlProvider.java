package com.finalproj.orbitflow.approval.document.documentContentRender;

import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : InternalImageUrlProvider
 * @since : 26. 1. 4. 일요일
 **/


@Component
public class InternalImageUrlProvider implements ImageUrlProvider {

    @Override
    public String generate(Long documentId, Long documentFileId) {
        return "/internal/pdf/documents/"
                + documentId
                + "/images/"
                + documentFileId;
    }
}
