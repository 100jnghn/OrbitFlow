package com.finalproj.orbitflow.chatbot.manual.service;

import com.finalproj.orbitflow.chatbot.manual.entity.ManualMetadata;
import com.finalproj.orbitflow.chatbot.manual.repository.ManualRepository;
import com.finalproj.orbitflow.chatbot.manualCategory.entity.ManualCategory;
import com.finalproj.orbitflow.chatbot.manualCategory.repository.ManualCategoryRepository;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.global.file.repository.FileRepository;
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
import org.springframework.context.annotation.Profile;
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
@Profile("chatbot")
public class ManualUploadService {

    private final ManualRepository manualMetadataRepository;
    private final ManualCategoryRepository manualCategoryRepository;
    private final EmployeeRepository employeeRepository;
    private final FileRepository fileRepository;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 특정 회사의 매뉴얼 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ManualMetadata> findAllByCompany(Long companyId) {
        // 최신 등록순으로 해당 회사의 매뉴얼 메타데이터를 가져옵니다.
        return manualMetadataRepository.findAllByCompanyIdOrderByIdDesc(companyId);
    }

    /**
     * 특정 회사의 특정 카테고리 매뉴얼 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ManualMetadata> findByCompanyAndCategory(Long companyId, Long categoryId) {
        return manualMetadataRepository.findByCompanyIdAndCategoryIdOrderByIdDesc(companyId, categoryId);
    }

    /**
     * 특정 회사의 활성 카테고리 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ManualCategory> findActiveCategoriesByCompany(Long companyId) {
        return manualCategoryRepository.findByCompanyIdAndIsActiveTrueOrderBySortOrderAsc(companyId);
    }

    /**
     * 매뉴얼 파일 업로드 및 벡터 인덱싱 (학습)
     */
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

            // 2. AI 학습 (벡터 인덱싱) - ChromaDB에 저장
            processVectorIndexing(file, category, employee);

            // 3. 매뉴얼 메타데이터 저장 (DB)
            saveManualMetadata(file, category, employee, savedFile);

        } catch (IOException e) {
            throw new InvalidRequestException("파일 처리 중 오류: " + e.getMessage());
        }
    }

    private File processFileStorage(MultipartFile file, Employee employee) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String systemFileName = UUID.randomUUID() + "_" + originalFileName;

        String objectKey = "manuals/" + systemFileName;

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Files.copy(file.getInputStream(), uploadPath.resolve(systemFileName), StandardCopyOption.REPLACE_EXISTING);

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

        // 텍스트를 500자 단위로 쪼개고 100자씩 겹치게 설정하여 문맥 유지
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 100);
        List<TextSegment> segments = splitter.split(document);

        // 메타데이터에 회사ID와 카테고리ID를 넣어 나중에 필터링 가능하게 함
        segments.forEach(segment -> {
            segment.metadata().add("company_id", employee.getCompany().getId());
            segment.metadata().add("category_id", category.getId());
        });

        // 벡터로 변환하여 ChromaDB에 저장
        embeddingStore.addAll(embeddingModel.embedAll(segments).content(), segments);
    }

    private void saveManualMetadata(MultipartFile file, ManualCategory category, Employee employee, File savedFile) {
        String fullPath = Paths.get(uploadDir).resolve(savedFile.getSysFile()).toString();

        ManualMetadata metadata = ManualMetadata.builder()
                .company(employee.getCompany())
                .category(category)
                .file(savedFile)
                .fileName(file.getOriginalFilename())
                .filePath(fullPath)
                .status("SUCCESS") // 학습 성공 상태로 저장
                .isActive(true)
                .build();

        manualMetadataRepository.save(metadata);
    }

    /**
     * 매뉴얼 삭제
     */
    @Transactional
    public void deleteManual(Long manualId, Long companyId) {
        ManualMetadata manual = manualMetadataRepository.findById(manualId)
                .orElseThrow(() -> new NotFoundException("매뉴얼을 찾을 수 없습니다."));

        // 회사 ID 검증
        if (!manual.getCompany().getId().equals(companyId)) {
            throw new InvalidRequestException("해당 매뉴얼을 삭제할 권한이 없습니다.");
        }

        manualMetadataRepository.delete(manual);
    }
}