package com.finalproj.orbitflow.approval.document.support.access;

import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentAccessValidator
 * @since : 26. 1. 21. 수요일
 **/


@Component
@RequiredArgsConstructor
public class DocumentAccessValidator {

    private final DocumentRepository documentRepository;

    public Document getForWriter(Employee employee, Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾지 못했습니다."));

        if (!document.getWriter().equals(employee)) {
            throw new ForbiddenException("작성자만 접근할 수 있습니다.");
        }
        return document;
    }

    public Document getForApprover(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾지 못했습니다."));
    }
}
