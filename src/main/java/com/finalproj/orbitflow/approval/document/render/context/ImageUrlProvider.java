package com.finalproj.orbitflow.approval.document.render.context;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : ImageUrlProvider
 * @since : 26. 1. 4. 일요일
 **/


@FunctionalInterface
public interface ImageUrlProvider {

    /**
     * @param documentId 문서 ID
     * @param documentFileId 파일 ID
     * @return 접근 가능한 이미지 URL
     */
    String generate(Long documentId, Long documentFileId);
}