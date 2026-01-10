package com.finalproj.orbitflow.chatbot.manual.service;

import com.finalproj.orbitflow.chatbot.manual.entity.ManualMetadata;
import com.finalproj.orbitflow.chatbot.manual.repository.ManualRepository;
import com.finalproj.orbitflow.chatbot.manualCategory.entity.ManualCategory;
import com.finalproj.orbitflow.chatbot.manualCategory.repository.ManualCategoryRepository;

import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.global.file.enums.FileDomain;
import com.finalproj.orbitflow.global.file.service.FileService;
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
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManualUploadService {

    private final ManualRepository manualMetadataRepository;
    private final ManualCategoryRepository manualCategoryRepository;
    private final EmployeeRepository employeeRepository;
    private final FileService fileService;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

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

        System.out.println("!!! 서비스 호출됨 - 파일명: " + file.getOriginalFilename());

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
            processVectorIndexing(file, category, employee, savedFile);

            // 3. 매뉴얼 메타데이터 저장 (DB)
            saveManualMetadata(file, category, employee, savedFile);

        } catch (IOException e) {
            throw new InvalidRequestException("파일 처리 중 오류: " + e.getMessage());
        }

    }

    private File processFileStorage(MultipartFile file, Employee employee) throws IOException {
        return fileService.upload(
                employee.getCompany().getId(),
                FileDomain.CHAT,
                file);
    }

    private void processVectorIndexing(MultipartFile file, ManualCategory category, Employee employee, File savedFile)
            throws IOException {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            log.error("[Vector Indexing] 실패: 파일명이 없습니다.");
            throw new InvalidRequestException("파일명이 없습니다.");
        }

        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.')).toLowerCase();

        // 1. 시작 로그
        log.info("[Vector Indexing] 프로세스 시작 - 파일명: {}, 확장자: {}, 회사ID: {}",
                originalFileName, fileExtension, employee.getCompany().getId());

        Document document;
        try {
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
                    log.error("[Vector Indexing] 실패: 지원하지 않는 확장자 ({})", fileExtension);
                    throw new InvalidRequestException("지원하지 않는 파일 형식입니다.");
            }

            // 2. 텍스트 추출 완료 로그
            log.info("[Vector Indexing] 텍스트 추출 완료 - 추출된 길이: {}자", document.text().length());

            // 텍스트를 500자 단위로 쪼개고 100자씩 겹치게 설정
            DocumentSplitter splitter = DocumentSplitters.recursive(500, 100);
            List<TextSegment> segments = splitter.split(document);

            // 3. 분할 완료 로그
            log.info("[Vector Indexing] 문장 분할 완료 - 생성된 세그먼트 수: {}개", segments.size());

            // 메타데이터 추가
            segments.forEach(segment -> {
                segment.metadata().add("company_id", employee.getCompany().getId().toString());
                segment.metadata().add("category_id", category.getId().toString());
                segment.metadata().add("file_id", savedFile.getId().toString()); // 파일 ID를 메타데이터로 저장
            });

            // 4. ChromaDB 저장 시도 로그
            log.info("[Vector Indexing] ChromaDB(벡터 DB) 저장 시도 중...");

            // 벡터로 변환하여 ChromaDB에 저장
            embeddingStore.addAll(embeddingModel.embedAll(segments).content(), segments);

            // 5. 최종 성공 로그
            log.info("[Vector Indexing] 최종 성공! ChromaDB에 데이터가 저장되었습니다. (파일명: {})", originalFileName);

        } catch (Exception e) {
            // 6. 실패 로그 (예외 발생 시)
            log.error("[Vector Indexing] 처리 중 오류 발생 - 파일명: {}, 사유: {}", originalFileName, e.getMessage(), e);
            throw e; // 예외를 다시 던져서 상위 트랜잭션 처리
        }
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

        ManualMetadata metadata = ManualMetadata.builder()
                .company(employee.getCompany())
                .category(category)
                .file(savedFile)
                .fileName(file.getOriginalFilename())
                .filePath(savedFile.getObjectKey()) // S3 Object Key 저장
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