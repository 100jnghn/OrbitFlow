package com.finalproj.orbitflow.hr.company.repository;

import com.finalproj.orbitflow.hr.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 회사(Company) 엔티티에 대한 Repository
 * - 회사 기본 정보 조회
 * - 회사명 / 사업자번호 중복 체크
 *
 * @author : seunga03
 * @filename : CompanyRepository
 * @since : 2025-12-16 화요일
 */
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * 회사명으로 회사 조회
     */
    Optional<Company> findByName(String name);

    /**
     * 사업자 번호로 회사 조회
     */
    Optional<Company> findByBusinessNumber(String businessNumber);

    /**
     * 회사명 중복 여부 확인
     */
    boolean existsByName(String name);

    /**
     * 사업자 번호 중복 여부 확인
     */
    boolean existsByBusinessNumber(String businessNumber);
}
