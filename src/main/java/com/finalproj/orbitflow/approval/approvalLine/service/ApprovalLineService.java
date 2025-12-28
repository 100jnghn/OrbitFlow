package com.finalproj.orbitflow.approval.approvalLine.service;

import com.finalproj.orbitflow.approval.approvalLine.dto.ApprovalRuleResDto;
import com.finalproj.orbitflow.approval.approvalLine.entity.ApprovalLine;
import com.finalproj.orbitflow.approval.approvalLine.repository.ApprovalLineRepository;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : ApprovalLineService
 * @since : 25. 12. 25. 목요일
 **/

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ApprovalLineService {

    private final ApprovalLineRepository approvalLineRepository;
    private final EmployeeRepository employeeRepository;
    private final DocumentRepository documentRepository;


    public List<ApprovalRuleResDto> getApprovalLinesByDocumentId(Long documentId) {

        if (!documentRepository.existsById(documentId)) {
            throw new NotFoundException("Document not found");
        }

        return approvalLineRepository
                .findByDocument_IdOrderByOrderNoAsc(documentId)
                .stream()
                .map(ApprovalRuleResDto::from)
                .toList();
    }

    @Transactional
    public void updateApprovalLine(Long approvalLineId, Long approvalId) {
        Employee approver = employeeRepository.findById(approvalId)
                .orElseThrow(() -> new NotFoundException("Approver not found"));

        ApprovalLine line = approvalLineRepository.findById(approvalLineId)
                .orElseThrow(() -> new NotFoundException("ApprovalLine not found"));

        line.setApprover(approver);
    }
}