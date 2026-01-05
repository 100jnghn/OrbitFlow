package com.finalproj.orbitflow.chatbot.manual.service;

import com.finalproj.orbitflow.chatbot.manual.entity.ManualMetadata;
import com.finalproj.orbitflow.chatbot.manual.repository.ManualRepository;
import com.finalproj.orbitflow.chatbot.manualCategory.entity.ManualCategory;
import com.finalproj.orbitflow.chatbot.manualCategory.repository.ManualCategoryRepository;

import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.global.file.enums.FileDomain;
import com.finalproj.orbitflow.global.file.repository.FileRepository;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

// HWP 파일 처리를 위한 import (라이브러리 추가 후 주석 해제)
// import kr.dogfoot.hwplib.object.HWPFile;
// import kr.dogfoot.hwplib.reader.HWPReader;
// import kr.dogfoot.hwplib.tool.textextractor.TextExtractMethod;
// import kr.dogfoot.hwplib.tool.textextractor.TextExtractor;

import java.io.IOException;
import java.io.InputStream;
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

    /**
     * 특정 회사의 매뉴얼 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ManualMetadata> findAllByCompany(Long companyId) {
        // 최신 등록순으로 해당 회사의 매뉴얼 메타데이터를 가져옵니다.
        return manualMetadataRepository.findAllByCompanyIdAndIsActiveTrueOrderByIdDesc(companyId);
    }

    /**
     * 특정 회사의 특정 카테고리 매뉴얼 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ManualMetadata> findByCompanyAndCategory(Long companyId, Long categoryId) {
        return manualMetadataRepository.findByCompanyIdAndCategoryIdAndIsActiveTrueOrderByIdDesc(companyId, categoryId);
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

        if (file.isEmpty()) {
            throw new InvalidRequestException("파일이 비어있습니다.");
        }
        
        // 파일 확장자 확인
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new InvalidRequestException("파일명이 없습니다.");
        }
        
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.')).toLowerCase();
        List<String> allowedExtensions = List.of(".txt", ".pdf", ".doc");
        
        if (!allowedExtensions.contains(fileExtension)) {
            throw new InvalidRequestException("TXT, PDF, DOC 파일만 업로드 가능합니다.");
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
                .domain(FileDomain.CHAT)
                .build());
    }

    private void processVectorIndexing(MultipartFile file, ManualCategory category, Employee employee) throws IOException {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new InvalidRequestException("파일명이 없습니다.");
        }
        
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.')).toLowerCase();
        Document document;
        
        // 파일 타입에 따라 적절한 파서 선택
        switch (fileExtension) {
            case ".pdf":
                DocumentParser pdfParser = new ApachePdfBoxDocumentParser();
                document = pdfParser.parse(file.getInputStream());
                break;
                
            case ".txt":
                DocumentParser txtParser = new TextDocumentParser();
                document = txtParser.parse(file.getInputStream());
                break;
                
            case ".doc":
                String docText = extractTextFromDoc(file.getInputStream());
                document = new Document(docText);
                break;
                
            default:
                throw new InvalidRequestException("지원하지 않는 파일 형식입니다.");
        }

        // 텍스트를 500자 단위로 쪼개고 100자씩 겹치게 설정하여 문맥 유지
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 100);
        List<TextSegment> segments = splitter.split(document);

        // 메타데이터에 회사ID와 카테고리ID를 넣어 나중에 필터링 가능하게 함
        segments.forEach(segment -> {
            segment.metadata().add("company_id", employee.getCompany().getId().toString());
            segment.metadata().add("category_id", category.getId().toString());
        });

        // 벡터로 변환하여 ChromaDB에 저장
        embeddingStore.addAll(embeddingModel.embedAll(segments).content(), segments);
    }
    
    /**
     * DOC 파일에서 텍스트 추출
     */
    private String extractTextFromDoc(InputStream inputStream) throws IOException {
        try {
            HWPFDocument document = new HWPFDocument(inputStream);
            WordExtractor extractor = new WordExtractor(document);
            String text = extractor.getText();
            extractor.close();
            document.close();
            return text;
        } catch (Exception e) {
            throw new IOException("DOC 파일 파싱 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
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

        manual.toggleActive();
        manual.updateStatus("DELETE");

    }
}