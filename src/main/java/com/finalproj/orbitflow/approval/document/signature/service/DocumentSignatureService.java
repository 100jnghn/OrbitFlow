package com.finalproj.orbitflow.approval.document.signature.service;

import com.finalproj.orbitflow.approval.line.entity.ApprovalLine;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.signature.entity.DocumentSignature;
import com.finalproj.orbitflow.approval.document.signature.repository.DocumentSignatureRepository;
import com.finalproj.orbitflow.global.image.signature.entity.EmployeeSignature;
import com.finalproj.orbitflow.global.image.signature.service.EmployeeSignatureService;
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
            return;
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

}
