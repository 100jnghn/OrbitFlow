package com.finalproj.orbitflow.approval.documentSignature.service;

import com.finalproj.orbitflow.approval.approvalLine.entity.ApprovalLine;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.documentSignature.entity.DocumentSignature;
import com.finalproj.orbitflow.approval.documentSignature.repository.DocumentSignatureRepository;
import com.finalproj.orbitflow.approval.employeeSignature.entity.EmployeeSignature;
import com.finalproj.orbitflow.approval.employeeSignature.service.EmployeeSignatureService;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentSignatureService
 * @since : 26. 1. 8. 목요일
 **/

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentSignatureService {
    private final DocumentSignatureRepository documentSignatureRepository;
    private final EmployeeSignatureService employeeSignatureService;

    @Transactional
    public void snapShotDocumentSignature(Document document, ApprovalLine approvalLine) {

        if (documentSignatureRepository
                .existsByDocument_IdAndApprovalLine_Id(
                        document.getId(),
                        approvalLine.getId()
                )) {
            return; // 이미 스냅샷 존재 → 무시
        }

        EmployeeSignature employeeSignature = employeeSignatureService.getEmployeeActiveSignature(approvalLine.getApprover().getId())
                .orElseThrow(() -> new NotFoundException("Not found Active Employee Signature. Employee Name : " + approvalLine.getApprover().getName()));


        DocumentSignature snapShot = DocumentSignature.builder()
                .document(document)
                .approvalLine(approvalLine)
                .company(approvalLine.getCompany())
                .signer(approvalLine.getApprover())
                .signatureFile(employeeSignature.getFile())
                .build();

        documentSignatureRepository.save(snapShot);
    }


    //public ResponseEntity<?> getSignatureImage(Long documentId, Long approvalLineId) {
    //    Document document = documentRepository.findById(documentId)
    //            .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));
    //
    //
    //}
}
