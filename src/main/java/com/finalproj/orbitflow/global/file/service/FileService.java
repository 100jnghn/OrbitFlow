package com.finalproj.orbitflow.global.file.service;

import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.global.file.dto.PresignedUrlResDto;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.global.file.enums.FileDomain;
import com.finalproj.orbitflow.global.file.repository.FileRepository;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
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

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        private final EmployeeRepository employeeRepository;

        @Value("${cloud.aws.s3.bucket}")
        private String bucket;

        @Transactional
        public File upload(Long companyId, Long uploaderId, FileDomain domain, MultipartFile multipartFile) {

                if (multipartFile == null || multipartFile.isEmpty()) {
                        return null;
                }

                Company company = companyRepository.findById(companyId)
                                .orElseThrow(() -> new NotFoundException("Company Not Found"));

                Employee uploader = employeeRepository.findById(uploaderId)
                                .orElseThrow(() -> new NotFoundException("Employee Not Found"));

                String origin = multipartFile.getOriginalFilename();
                if (origin == null || origin.isBlank()) {
                        origin = "unknown";
                }

                String objectKey = createObjectKey(company.getId(), domain, origin);

                try {
                        // 1️⃣ S3 업로드 (외부 시스템)
                        s3Client.putObject(
                                        PutObjectRequest.builder()
                                                        .bucket(bucket)
                                                        .key(objectKey)
                                                        .contentType(multipartFile.getContentType())
                                                        .build(),
                                        RequestBody.fromInputStream(
                                                        multipartFile.getInputStream(),
                                                        multipartFile.getSize()));

                        // 2️⃣ DB 저장
                        File file = File.builder()
                                        .company(company)
                                        .domain(domain)
                                        .objectKey(objectKey)
                                        .originFile(origin)
                                        .sysFile(extractSysFile(objectKey))
                                        .contentType(multipartFile.getContentType())
                                        .fileSize(multipartFile.getSize())
                                        .build();

                        return fileRepository.save(file);

                } catch (Exception e) {
                        // 3️⃣ DB 실패 시 S3 롤백 (보상 트랜잭션)
                        deleteObject(objectKey);
                        throw new RuntimeException("파일 업로드 처리 중 오류 발생", e);
                }
        }

        public ResponseEntity<Resource> download(Long fileId, Long companyId) {
                File file = fileRepository.findByIdAndCompany_Id(fileId, companyId)
                                .orElseThrow(() -> new NotFoundException("파일을 찾을 수 없습니다. id=" + fileId));

                ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(
                                GetObjectRequest.builder()
                                                .bucket(bucket)
                                                .key(file.getObjectKey())
                                                .build());

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
                                                                UriUtils.encode(file.getOriginFile(),
                                                                                StandardCharsets.UTF_8)
                                                                + "\"")
                                .build();

                PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(
                                GetObjectPresignRequest.builder()
                                                .signatureDuration(Duration.ofMinutes(5))
                                                .getObjectRequest(getObjectRequest)
                                                .build());

                return new PresignedUrlResDto(
                                presignedRequest.url().toString(),
                                LocalDateTime.now().plusMinutes(5));
        }

        /*
         * helper
         */
        /**
         * S3 파일 삭제
         *
         * @param objectKey S3 객체 키
         */
        public void deleteObject(String objectKey) {
                try {
                        s3Client.deleteObject(
                                        DeleteObjectRequest.builder()
                                                        .bucket(bucket)
                                                        .key(objectKey)
                                                        .build());
                } catch (Exception ex) {
                        log.error("S3 delete failed. objectKey={}", objectKey, ex);
                }
        }

        private String createObjectKey(Long companyId, FileDomain domain, String originFileName) {
                String uuid = UUID.randomUUID().toString();
                String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
                return "company/%d/%s/%s/%s_%s".formatted(companyId, domain.getPath(), datePath, uuid, originFileName);
        }

        private String extractSysFile(String objectKey) {
                return objectKey.substring(objectKey.lastIndexOf('/') + 1);
        }

}
