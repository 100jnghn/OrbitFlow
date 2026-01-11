package com.finalproj.orbitflow.global.file.repository;

import com.finalproj.orbitflow.global.file.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FileRepository
 * @since : 26. 1. 1. 목요일
 **/


public interface FileRepository extends JpaRepository<File,Long> {
    Optional<File> findByIdAndCompany_Id(Long id, Long companyId);
}
