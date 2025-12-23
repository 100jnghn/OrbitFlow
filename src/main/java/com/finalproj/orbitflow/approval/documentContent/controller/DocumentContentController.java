package com.finalproj.orbitflow.approval.documentContent.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentContentController
 * @since : 25. 12. 23. 화요일
 **/

@RestController
@RequestMapping("/api/document-contents")
@RequiredArgsConstructor
public class DocumentContentController {


    //TODO 값이 입력된 문서 구조를 JSON으로 전달받아 documentContent 생성
    @PostMapping
    public ResponseEntity<ResponseDto> createContent() {

        return null;
    }
}
