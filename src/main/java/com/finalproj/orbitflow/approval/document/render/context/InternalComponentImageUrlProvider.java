package com.finalproj.orbitflow.approval.document.render.context;

import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : InternalComponentImageUrlProvider
 * @since : 26. 1. 4. 일요일
 **/


@Component
public class InternalComponentImageUrlProvider implements ImageUrlProvider {

    @Override
    public String generate(Long documentId, Long documentFileId) {
        return "/internal/pdf/documents/"
                + documentId
                + "/component/images/"
                + documentFileId;
    }
}
