package com.finalproj.orbitflow.chatbot.manual.service;

import com.finalproj.orbitflow.chatbot.chorma.service.ChromaVectorService;
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

/**
 *
 * @author : rlagkdus
 * @filename : ManualUploadService
 * @since : 2025. 12. 30. 화요일
 */


@Service
@RequiredArgsConstructor
@Slf4j
public class ManualUploadService {

    private final ManualRepository manualMetadataRepository;
    private final ManualCategoryRepository manualCategoryRepository;
    private final EmployeeRepository employeeRepository;
    private final ManualRepository manualRepository;
    private final FileService fileService;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ChromaVectorService chromaVectorService;

    @Transactional(readOnly = true)
    public List<ManualMetadata> findAllByCompany(Long companyId) {
        return manualMetadataRepository.findAllByCompanyIdAndIsActiveTrueOrderByIdDesc(companyId);
    }


    @Transactional(readOnly = true)
    public List<ManualMetadata> findByCompanyAndCategory(Long companyId, Long categoryId) {
        return manualMetadataRepository.findByCompanyIdAndCategoryIdAndIsActiveTrueOrderByIdDesc(companyId, categoryId);
    }

    @Transactional(readOnly = true)
    public List<ManualCategory> findActiveCategoriesByCompany(Long companyId) {
        return manualCategoryRepository.findByCompanyIdAndIsActiveTrueOrderBySortOrderAsc(companyId);
    }

    @Transactional
    public void uploadAndIndexingManual(MultipartFile file, Long categoryId, Long employeeId) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("사원 정보를 찾을 수 없습니다."));

        ManualCategory category = manualCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new InvalidRequestException("존재하지 않는 카테고리입니다."));

        if (file.isEmpty()) {
            throw new InvalidRequestException("파일이 비어있습니다.");
        }

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
            File savedFile = processFileStorage(file, employee);
            processVectorIndexing(file, category, employee, savedFile);

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
            throw new InvalidRequestException("파일명이 없습니다.");
        }

        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.')).toLowerCase();

        Document document;
        try {
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

            DocumentSplitter splitter = DocumentSplitters.recursive(500, 100);
            List<TextSegment> segments = splitter.split(document);

            segments.forEach(segment -> {
                segment.metadata().add("company_id", employee.getCompany().getId().toString());
                segment.metadata().add("category_id", category.getId().toString());
                segment.metadata().add("file_id", savedFile.getId().toString()); // 파일 ID를 메타데이터로 저장
            });

            embeddingStore.addAll(embeddingModel.embedAll(segments).content(), segments);
        } catch (Exception e) {
            throw e;
        }
    }


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
                .filePath(savedFile.getObjectKey())
                .status("SUCCESS")
                .isActive(true)
                .build();

        manualMetadataRepository.save(metadata);
    }


    @Transactional
    public void deleteManual(Long manualId, Long companyId) {

        ManualMetadata manual = manualMetadataRepository.findById(manualId)
                .orElseThrow(() -> new NotFoundException("매뉴얼을 찾을 수 없습니다."));

        if (!manual.getCompany().getId().equals(companyId)) {
            throw new InvalidRequestException("해당 매뉴얼을 삭제할 권한이 없습니다.");
        }

        manual.deactivate();
        manual.updateStatus("DELETE");


        String fileId = manual.getFile().getId().toString();
        try {
            chromaVectorService.deleteByFileId(companyId, fileId);
        } catch (Exception e) {

        }
    }


    @Transactional
    public void updateManualActive(Long companyId, Long manualId, boolean isActive) {
        ManualMetadata manual = manualRepository
                .findByIdAndCompanyId(manualId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("매뉴얼이 없거나 권한이 없습니다."));

        if (isActive) {
            manual.activate();
        } else {
            manual.deactivate();
        }
    }
}