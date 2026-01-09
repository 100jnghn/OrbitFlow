package com.finalproj.orbitflow.approval.employeeSignature.repository;

import com.finalproj.orbitflow.approval.employeeSignature.entity.EmployeeSignature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : EmployeeSignatureRepository
 * @since : 26. 1. 8. 목요일
 **/


public interface EmployeeSignatureRepository extends JpaRepository<EmployeeSignature, Long> {

    Optional<EmployeeSignature> findByEmployee_IdAndIsActive(Long employeeId, boolean isActive);
}
