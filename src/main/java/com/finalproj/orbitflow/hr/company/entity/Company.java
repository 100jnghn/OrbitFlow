package com.finalproj.orbitflow.hr.company.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회사 정보를 관리하는 엔티티.
 * - 멀티테넌트 환경에서 하나의 회사(테넌트)를 의미한다.
 * - 조직, 사원, 직급, 직책 등 모든 인사 데이터의 최상위 기준 엔티티이다.
 * - 생성/수정 일시는 BaseEntity를 통해 Auditing으로 자동 관리된다.
 *
 * @author : seunga03
 * @filename : Company
 * @since : 2025-12-15 월요일
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "company",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "name"),
                @UniqueConstraint(columnNames = "business_number")
        }
)
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 회사 ID (PK)

    /* ==============================
       회사 기본 정보
       ============================== */

    @Column(nullable = false, length = 100)
    private String name; // 회사명

    @Column(name = "business_number", nullable = false, length = 20)
    private String businessNumber; // 사업자 번호

    @Column(nullable = false)
    private String address; // 주소

    /* ==============================
       대표자 정보
       ============================== */

    @Column(name = "representative_name", nullable = false, length = 50)
    private String representativeName; // 대표자명

    @Column(name = "representative_contact", nullable = false, length = 20)
    private String representativeContact; // 대표자 연락처

    /**
     * 회사 생성
     **/
    public static Company create(
            String name,
            String businessNumber,
            String address,
            String representativeName,
            String representativeContact
    ) {
        Company company = new Company();
        company.name = name;
        company.businessNumber = businessNumber;
        company.address = address;
        company.representativeName = representativeName;
        company.representativeContact = representativeContact;
        return company;
    }

}