package com.finalproj.orbitflow.board.boardCategory.service;

import com.finalproj.orbitflow.board.boardCategory.entity.BoardCategory;
import com.finalproj.orbitflow.board.boardCategory.repository.BoardCategoryRepository;
import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.organization.entity.Organization;
import com.finalproj.orbitflow.hr.organization.repository.OrgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationBoardCategorySyncService {

    private final BoardCategoryRepository boardCategoryRepository;
    private final OrgRepository orgRepository;
    private final CompanyRepository companyRepository;

    /** 조직 생성 시: 조직게시판 없으면 자동 생성 */
    @Transactional
    public void createIfAbsent(Long companyId, Long organizationId, String orgName) {

        Organization org = orgRepository.findById(organizationId)
                .orElseThrow(() -> new NotFoundException("조직을 찾을 수 없습니다."));

        // Organization이 companyId(Long)만 가진 구조라면 이렇게 비교
        if (!org.getCompanyId().equals(companyId)) {
            throw new ForbiddenException("해당 회사의 조직이 아닙니다.");
        }

        boolean exists = boardCategoryRepository
                .existsByCompany_IdAndOrganization_IdAndDeletedAtIsNull(companyId, organizationId);
        if (exists) return;

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("회사를 찾을 수 없습니다."));

        BoardCategory category = BoardCategory.builder()
                .company(company)
                .organization(org)
                .boardName(orgName + " 게시판")
                .boardType("ORGANIZATION")   // 프로젝트 정책에 맞춰 상수/enum으로 통일 추천
                .isActivated(true)
                .commentActivated(true)
                .build();

        boardCategoryRepository.save(category);
    }

    /** 조직명 변경 시: 조직게시판 이름 동기화 */
    @Transactional
    public void syncBoardName(Long companyId, Long organizationId, String newOrgName) {

        BoardCategory category = boardCategoryRepository
                .findByOrganization_IdAndDeletedAtIsNull(organizationId)
                .orElseThrow(() -> new NotFoundException("조직 게시판이 존재하지 않습니다."));

        if (!category.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException("해당 게시판에 대한 권한이 없습니다.");
        }
        if (category.getOrganization() == null) return;

        category.update(
                newOrgName + " 게시판",
                category.getBoardType(),
                category.isActivated(),
                category.isCommentActivated()
        );
    }

    /** 조직 비활성화 시: 조직게시판도 비활성화 */
    @Transactional
    public void deactivateBoard(Long companyId, Long organizationId) {

        BoardCategory category = boardCategoryRepository
                .findByOrganization_IdAndDeletedAtIsNull(organizationId)
                .orElseThrow(() -> new NotFoundException("조직 게시판이 존재하지 않습니다."));

        if (!category.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException("해당 게시판에 대한 권한이 없습니다.");
        }
        if (category.getOrganization() == null) return;

        category.update(
                category.getBoardName(),
                category.getBoardType(),
                false,
                category.isCommentActivated()
        );
    }
}