package com.finalproj.orbitflow.global.file.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.file.dto.FileUploadResDto;
import com.finalproj.orbitflow.global.file.dto.PresignedUrlResDto;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.global.file.enums.FileDomain;
import com.finalproj.orbitflow.global.file.service.FileService;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FileController
 * @since : 26. 1. 1. 목요일
 **/

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    @PostMapping
    public ResponseEntity<FileUploadResDto> upload(
            @RequestParam FileDomain domain,
            @RequestPart MultipartFile file
    ) {
        File saved = fileService.upload(
                SecurityUtils.getCompanyId(),
                SecurityUtils.getEmployeeId(),
                domain,
                file
        );

        return ResponseEntity.ok(FileUploadResDto.from(saved));
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long fileId) {
        return fileService.download(fileId, SecurityUtils.getCompanyId());
    }

    @GetMapping("/{fileId}/presigned")
    public ResponseEntity<ResponseDto> presigned(@PathVariable Long fileId) {
        PresignedUrlResDto result = fileService.createPresignedDownloadUrl(SecurityUtils.getCompanyId(), fileId);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "임시 다운로드 url 반환", result));
    }

}
