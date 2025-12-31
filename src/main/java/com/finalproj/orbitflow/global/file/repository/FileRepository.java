package com.finalproj.orbitflow.global.file.repository;

import com.finalproj.orbitflow.global.file.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : FileRepository
 * @since : 2025. 12. 30. 화요일
 */
public interface FileRepository extends JpaRepository<File,Long> {
}
