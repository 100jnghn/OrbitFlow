package com.finalproj.orbitflow.global.file.storage;

import org.springframework.core.io.Resource;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FileStorage
 * @since : 26. 1. 3. 토요일
 **/


public interface FileStorage {

    /**
     * 파일을 스트리밍 리소스로 로드한다 (PDF, 이미지 등 내부용)
     */
    Resource loadAsResource(String objectKey);

}
