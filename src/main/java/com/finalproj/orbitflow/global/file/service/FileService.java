package com.finalproj.orbitflow.global.file.service;

import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.global.file.dto.PresignedUrlResDto;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.global.file.enums.FileDomain;
import com.finalproj.orbitflow.global.file.repository.FileRepository;
import com.finalproj.orbitflow.global.file.storage.FileStorage;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FileService
 * @since : 26. 1. 1. 목요일
 **/

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FileService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final FileRepository fileRepository;
    private final CompanyRepository companyRepository;
    private final FileStorage fileStorage;


    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Transactional
    public File upload(
            Long companyId,
            FileDomain domain,
            MultipartFile multipartFile
    ) {


        Company company = companyRepository.findById(companyId)
                .orElseThrow();


        String origin = Optional.ofNullable(multipartFile.getOriginalFilename())
                .filter(s -> !s.isBlank())
                .orElse("unknown");

        try {
            return saveFileInternal(
                    company,
                    domain,
                    origin,
                    multipartFile.getContentType(),
                    multipartFile.getSize(),
                    multipartFile.getInputStream()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Transactional
    public File saveGeneratedPdf(
            Long companyId,
            Long documentId,
            byte[] pdfBytes
    ) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow();


        String fileName = "document-" + documentId + ".pdf";

        return saveFileInternal(
                company,
                FileDomain.DOCUMENT_PDF,
                fileName,
                "application/pdf",
                pdfBytes.length,
                new ByteArrayInputStream(pdfBytes)
        );
    }


    private File saveFileInternal(
            Company company,
            FileDomain domain,
            String originFileName,
            String contentType,
            long size,
            InputStream inputStream
    ) {
        String objectKey = createObjectKey(company.getId(), domain, originFileName);

        try {
            // 1️⃣ S3 업로드
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(inputStream, size)
            );

            // 2️⃣ DB 저장
            File file = File.builder()
                    .company(company)
                    .domain(domain)
                    .objectKey(objectKey)
                    .originFile(originFileName)
                    .sysFile(extractSysFile(objectKey))
                    .contentType(contentType)
                    .fileSize(size)
                    .build();

            File savedFile = fileRepository.save(file);

            // 3️⃣ 트랜잭션 보상
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronization() {
                            @Override
                            public void afterCompletion(int status) {
                                if (status == STATUS_ROLLED_BACK) {
                                    deleteObject(objectKey);
                                }
                            }
                        }
                );
            }
            return savedFile;
        } catch (Exception e) {
            deleteObject(objectKey);
            throw new RuntimeException("파일 저장 중 오류 발생", e);
        }
    }


    public ResponseEntity<Resource> download(Long fileId, Long companyId) {
        File file = fileRepository.findByIdAndCompany_Id(fileId, companyId)
                .orElseThrow(() -> new NotFoundException("파일을 찾을 수 없습니다. id=" + fileId));

        ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(file.getObjectKey())
                        .build()
        );

        InputStreamResource resource = new InputStreamResource(s3Object);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (file.getContentType() != null && !file.getContentType().isBlank()) {
            mediaType = MediaType.parseMediaType(file.getContentType());
        }

        String encodedName = UriUtils.encode(file.getOriginFile(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedName + "\"")
                .contentLength(file.getFileSize() != null ? file.getFileSize() : -1)
                .body(resource);
    }

    public PresignedUrlResDto createPresignedDownloadUrl(Long companyId, Long fileId) {
        File file = fileRepository.findByIdAndCompany_Id(fileId, companyId)
                .orElseThrow(() -> new NotFoundException("파일을 찾을 수 없습니다. id=" + fileId));

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(file.getObjectKey())
                .responseContentDisposition(
                        "attachment; filename=\"" +
                                UriUtils.encode(file.getOriginFile(), StandardCharsets.UTF_8) + "\""
                )
                .build();

        PresignedGetObjectRequest presignedRequest =
                s3Presigner.presignGetObject(
                        GetObjectPresignRequest.builder()
                                .signatureDuration(Duration.ofMinutes(5))
                                .getObjectRequest(getObjectRequest)
                                .build()
                );

        return new PresignedUrlResDto(
                presignedRequest.url().toString(),
                LocalDateTime.now().plusMinutes(5)
        );
    }

    public void deleteObject(String objectKey) {
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .build()
            );
        } catch (Exception ex) {
            log.error("S3 delete failed. objectKey={}", objectKey, ex);
        }
    }

    public ResponseEntity<byte[]> streamImage(File file) {

        byte[] bytes = downloadFromS3(file.getObjectKey());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(bytes);
    }

    /*
     * helper
     * */

    private String createObjectKey(Long companyId, FileDomain domain, String originFileName) {
        String uuid = UUID.randomUUID().toString();
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        return "company/%d/%s/%s/%s_%s".formatted(companyId, domain.getPath(), datePath, uuid, originFileName);
    }

    private String extractSysFile(String objectKey) {
        return objectKey.substring(objectKey.lastIndexOf('/') + 1);
    }


    private byte[] downloadFromS3(String objectKey) {
        ResponseInputStream<GetObjectResponse> s3Object =
                s3Client.getObject(
                        GetObjectRequest.builder()
                                .bucket(bucket)
                                .key(objectKey)
                                .build()
                );

        try {
            return s3Object.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("이미지 다운로드 실패", e);
        }
    }

    public Resource loadAsResource(File file) {
        return fileStorage.loadAsResource(file.getObjectKey());
    }
}