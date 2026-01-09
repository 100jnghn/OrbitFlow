package com.finalproj.orbitflow.approval.document.service;

import com.finalproj.orbitflow.approval.approvalLine.entity.ApprovalLine;
import com.finalproj.orbitflow.approval.approvalLine.repository.ApprovalLineRepository;
import com.finalproj.orbitflow.approval.document.dto.PdfApprovalLineDto;
import com.finalproj.orbitflow.approval.document.dto.PdfApproverDto;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : PdfApprovalLineAssembler
 * @since : 26. 1. 5. 월요일
 **/


@Component
@RequiredArgsConstructor
public class PdfApprovalLineAssembler {

    private final ApprovalLineRepository approvalLineRepository;
    //private final SignatureUrlResolver signatureUrlResolver;

    public PdfApprovalLineDto from(Long documentId) {

        List<ApprovalLine> approvals =
                approvalLineRepository.findByDocument_IdOrderByOrderNoAsc(documentId);

        if (approvals.isEmpty()) {
            return new PdfApprovalLineDto(List.of());
        }

        List<PdfApproverDto> approvers = approvals.stream()
                .map(this::toApproverDto)
                .toList();

        return new PdfApprovalLineDto(approvers);
    }

    private String resolvePosition(Employee employee) {
        // 직책 > 직급 > 조직명 등 정책에 맞게
        if (employee.getPositionCategory() != null) {
            return employee.getPositionCategory().getName();
        }
        if (employee.getRank() != null) {
            return employee.getRank().getName();
        }
        return "";
    }

    //private String resolveSignatureUrl(ApprovalLine approvalLine) {
    //    if (approvalLine.getStatus() != ApprovalStatus.APPROVED) {
    //        return null;
    //    }
    //    return signatureUrlResolver.resolve(approvalLine);
    //}

    private PdfApproverDto toApproverDto(ApprovalLine approvalLine) {

        Employee approver = approvalLine.getApprover();

        String signatureImageUrl = null;

        // 승인 + 서명 파일이 존재할 때만 이미지 노출
        //if (approvalLine.getStatus().equals(ApprovalStatus.APPROVED) && approvalLine.getSignatureFile() != null) {
        //    signatureImageUrl =
        //            "/api/documents/"
        //                    + approvalLine.getDocument().getId()
        //                    + "/approvals/"
        //                    + approvalLine.getId()
        //                    + "/signature";
        //}

        return new PdfApproverDto(
                approvalLine.getId(),
                approvalLine.getOrderNo(),
                approver.getName(),
                approver.getPositionCategory() != null
                        ? approver.getPositionCategory().getName()
                        : null,
                signatureImageUrl
        );
    }
}
