package com.finalproj.orbitflow.chatbot.manual.service;

import com.finalproj.orbitflow.chatbot.manual.entity.ManualMetadata;
import com.finalproj.orbitflow.chatbot.manual.repository.ManualRepository;
import com.finalproj.orbitflow.chatbot.manualCategory.entity.ManualCategory;
import com.finalproj.orbitflow.chatbot.manualCategory.repository.ManualCategoryRepository;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.global.file.repository.FileRepository;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManualUploadService {

    private final ManualRepository manualMetadataRepository;
    private final ManualCategoryRepository manualCategoryRepository;
    private final EmployeeRepository employeeRepository;
    private final FileRepository fileRepository;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Transactional
    public void uploadAndIndexingManual(MultipartFile file, Long categoryId, Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("사원 정보를 찾을 수 없습니다."));

        ManualCategory category = manualCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new InvalidRequestException("존재하지 않는 카테고리입니다."));

        if (file.isEmpty() || !file.getContentType().equals("application/pdf")) {
            throw new InvalidRequestException("올바른 PDF 파일을 업로드해주세요.");
        }

        try {
            // 1. 맥북 로컬 경로에 파일 물리 저장 및 File 엔티티 생성
            File savedFile = processFileStorage(file, employee);

            // 2. AI 학습 (벡터 인덱싱)
            processVectorIndexing(file, category, employee);

            // 3. 매뉴얼 메타데이터 저장
            saveManualMetadata(file, category, employee, savedFile);

        } catch (IOException e) {
            throw new InvalidRequestException("파일 처리 중 오류: " + e.getMessage());
        }
    }

    private File processFileStorage(MultipartFile file, Employee employee) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String systemFileName = UUID.randomUUID() + "_" + originalFileName;

        // S3 전환을 대비한 Object Key 구조 (폴더 경로 포함)
        String objectKey = "manuals/" + systemFileName;

        // 맥북 경로 처리
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath); // 폴더가 없으면 생성
        }

        // 파일 복사
        Files.copy(file.getInputStream(), uploadPath.resolve(systemFileName), StandardCopyOption.REPLACE_EXISTING);

        // object_key 포함하여 DB 저장
        return fileRepository.save(File.builder()
                .company(employee.getCompany())
                .originFile(originalFileName)
                .sysFile(systemFileName)
                .objectKey(objectKey)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build());
    }

    private void processVectorIndexing(MultipartFile file, ManualCategory category, Employee employee) throws IOException {
        DocumentParser parser = new ApachePdfBoxDocumentParser();
        Document document = parser.parse(file.getInputStream());

        DocumentSplitter splitter = DocumentSplitters.recursive(500, 100);
        List<TextSegment> segments = splitter.split(document);

        segments.forEach(segment -> {
            segment.metadata().add("company_id", employee.getCompany().getId());
            segment.metadata().add("category_id", category.getId());
        });

        // 벡터 변환 및 저장
        embeddingStore.addAll(embeddingModel.embedAll(segments).content(), segments);
    }

    private void saveManualMetadata(MultipartFile file, ManualCategory category, Employee employee, File savedFile) {
        // 맥북의 물리적 경로를 포함하여 저장
        String fullPath = Paths.get(uploadDir).resolve(savedFile.getSysFile()).toString();

        ManualMetadata metadata = ManualMetadata.builder()
                .company(employee.getCompany())
                .category(category)
                .file(savedFile)
                .fileName(file.getOriginalFilename())
                .filePath(fullPath)
                .status("SUCCESS")
                .isActive(true)
                .build();

        manualMetadataRepository.save(metadata);
    }
}