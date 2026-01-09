package com.finalproj.orbitflow.approval.employeeSignature.service;

import com.finalproj.orbitflow.approval.approvalLine.repository.ApprovalLineRepository;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.employeeSignature.entity.EmployeeSignature;
import com.finalproj.orbitflow.approval.employeeSignature.repository.EmployeeSignatureRepository;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.global.file.enums.FileDomain;
import com.finalproj.orbitflow.global.file.service.FileService;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : EmployeeSignatureService
 * @since : 26. 1. 8. 목요일
 **/

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeSignatureService {
    private final EmployeeSignatureRepository employeeSignatureRepository;
    private final FileService fileService;
    private final EmployeeRepository employeeRepository;
    private final DocumentRepository documentRepository;
    private final ApprovalLineRepository approvalLineRepository;

    public boolean hasActiveSignature(Long employeeId) {
        return getEmployeeActiveSignature(employeeId).isPresent();
    }


    @Transactional
    public void saveSignature(Long employeeId, MultipartFile multipartFile) {

        String contentType = multipartFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidRequestException("이미지 파일만 업로드할 수 있습니다.");
        }


        Employee employee = employeeRepository.findByIdAndStatus(employeeId, EmployeeStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Not fount employee. Id : " + employeeId));

        Optional<EmployeeSignature> curActiveSig = getEmployeeActiveSignature(employeeId);

        curActiveSig.ifPresent(sig -> {
            sig.deactivate();
            employeeSignatureRepository.save(sig);
            employeeSignatureRepository.flush();
        });

        File file = fileService.upload(
                employee.getCompany().getId(),
                FileDomain.SIGNATURE,
                multipartFile
        );

        EmployeeSignature employeeSignature = EmployeeSignature.builder()
                .company(employee.getCompany())
                .employee(employee)
                .file(file)
                .isActive(true)
                .build();

        employeeSignatureRepository.save(employeeSignature);
    }

    public Optional<EmployeeSignature> getEmployeeActiveSignature(Long employeeId) {
        return employeeSignatureRepository.findByEmployee_IdAndIsActive(employeeId, true);
    }

}
