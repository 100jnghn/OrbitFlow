package com.finalproj.orbitflow.board.boardCategory.service;

import com.finalproj.orbitflow.board.boardCategory.dto.BoardCategoryReqDto;
import com.finalproj.orbitflow.board.boardCategory.dto.BoardCategoryResDto;
import com.finalproj.orbitflow.board.boardCategory.entity.BoardCategory;
import com.finalproj.orbitflow.board.boardCategory.repository.BoardCategoryRepository;
import com.finalproj.orbitflow.board.boardPermission.repository.BoardPermissionRepository;
import com.finalproj.orbitflow.global.exception.BusinessException;
import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.organization.repository.OrgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminBoardCategoryService {

        private final BoardCategoryRepository boardCategoryRepository;
        private final CompanyRepository companyRepository;
        private final OrgRepository orgRepository;
        private final BoardPermissionRepository boardPermissionRepository;

        // =========================================================================
        // 1. 일반 카테고리 관리
        // =========================================================================

        /** [관리자용] 게시판 카테고리 목록 조회 (일반 / 조직) */
        public Page<BoardCategoryResDto.Category> getBoardCategoryList(
                        Long companyId,
                        boolean organizationOnly,
                        Pageable pageable) {
                Page<BoardCategory> page = organizationOnly
                                ? boardCategoryRepository
                                                .findByCompany_IdAndOrganizationIsNotNullAndDeletedAtIsNull(companyId,
                                                                pageable)
                                : boardCategoryRepository
                                                .findAllByCompany_IdAndOrganizationIsNullAndDeletedAtIsNull(companyId,
                                                                pageable);

                return page.map(BoardCategoryResDto.Category::from);
        }

        /** [관리자용] 일반 카테고리 단건 조회 (수정 페이지 로딩용) */
        public BoardCategoryResDto.Detail getCategoryDetail(
                        Long companyId,
                        Long categoryId) {
                BoardCategory category = getVerifiedCategory(companyId, categoryId);
                return BoardCategoryResDto.Detail.from(category);
        }

        /** [관리자용] 게시판 카테고리 생성 */
        @Transactional
        public Long createCategory(Long companyId, BoardCategoryReqDto.Create dto) {

                Company company = companyRepository.findById(companyId)
                                .orElseThrow(() -> new NotFoundException("존재하지 않는 회사입니다."));

                if ("NOTICE".equals(dto.getBoardType()) || "공지사항".equals(dto.getBoardType())) {
                        boolean existsNotice = boardCategoryRepository
                                        .existsByCompany_IdAndBoardTypeAndDeletedAtIsNull(companyId, "NOTICE");
                        boolean existsKoreanNotice = boardCategoryRepository
                                        .existsByCompany_IdAndBoardTypeAndDeletedAtIsNull(companyId, "공지사항");
                        if (existsNotice || existsKoreanNotice) {
                                throw new BusinessException("공지 게시판은 이미 존재합니다.\n공지 게시판은 하나만 생성할 수 있습니다.");
                        }
                }

                BoardCategory category = BoardCategory.builder()
                                .company(company)
                                .organization(
                                                dto.getOrganizationId() == null
                                                                ? null
                                                                : orgRepository.findById(dto.getOrganizationId())
                                                                                .orElseThrow(() -> new NotFoundException(
                                                                                                "존재하지 않는 조직입니다.")))
                                .boardName(dto.getBoardName())
                                .boardType(dto.getBoardType())
                                .isActivated(dto.getIsActivated())
                                .commentActivated(dto.getCommentActivated())
                                .build();

                return boardCategoryRepository.save(category).getId();
        }

        /** [관리자용] 게시판 카테고리 수정 */
        @Transactional
        public void updateCategory(
                        Long companyId,
                        Long categoryId,
                        BoardCategoryReqDto.Update dto) {
                BoardCategory category = getVerifiedCategory(companyId, categoryId);

                if ("NOTICE".equals(dto.getBoardType()) || "공지사항".equals(dto.getBoardType())) {
                        if (!"NOTICE".equals(category.getBoardType()) && !"공지사항".equals(category.getBoardType())) {
                                boolean existsNotice = boardCategoryRepository
                                                .existsByCompany_IdAndBoardTypeAndDeletedAtIsNull(companyId, "NOTICE");
                                boolean existsKoreanNotice = boardCategoryRepository
                                                .existsByCompany_IdAndBoardTypeAndDeletedAtIsNull(companyId, "공지사항");
                                if (existsNotice || existsKoreanNotice) {
                                        throw new BusinessException("공지 게시판은 이미 존재합니다.\n공지 게시판은 하나만 생성할 수 있습니다.");
                                }
                        }
                }

                // if ("NOTICE".equals(category.getBoardType())) {
                // throw new BusinessException("공지사항 게시판은 수정할 수 없습니다.");
                // }

                category.update(
                                dto.getBoardName(),
                                dto.getBoardType(),
                                dto.getIsActivated() != null
                                                ? dto.getIsActivated()
                                                : category.isActivated(),
                                dto.getCommentActivated());
        }

        /** [관리자용] 게시판 카테고리 삭제 (소프트 삭제, 공지사항은 제외) */
        @Transactional
        public void deleteCategory(Long companyId, Long categoryId) {

                BoardCategory category = getVerifiedCategory(companyId, categoryId);

                // if ("NOTICE".equals(category.getBoardType())) {
                // throw new BusinessException("공지사항 게시판은 삭제할 수 없습니다.");
                // }

                if (category.isDeleted()) {
                        throw new BusinessException("이미 삭제된 게시판입니다.");
                }

                category.softDelete();
        }

        // =========================================================================
        // 2. 조직 게시판 관리
        // =========================================================================

        /** [관리자용] 조직 게시판 활성/비활성 토글 */
        @Transactional
        public void changeOrganizationBoardActivation(
                        Long companyId,
                        Long categoryId,
                        boolean isActivated) {
                BoardCategory category = getVerifiedCategory(companyId, categoryId);

                if (category.getOrganization() == null) {
                        throw new BusinessException("조직 게시판만 활성화 변경이 가능합니다.");
                }

                category.update(
                                category.getBoardName(),
                                category.getBoardType(),
                                isActivated,
                                category.isCommentActivated());
        }

        /** [사용자용] 본인 조직 게시판 조회 (활성화된 게시판만) */
        public List<BoardCategoryResDto.Category> getActiveOrganizationBoards(Long orgId) {
                return boardCategoryRepository
                                .findByOrganization_IdAndIsActivatedTrueAndDeletedAtIsNull(orgId)
                                .stream()
                                .map(BoardCategoryResDto.Category::from)
                                .toList();
        }

        // =========================================================================
        // 공통 검증 메서드
        // =========================================================================

        private BoardCategory getVerifiedCategory(Long companyId, Long categoryId) {

                BoardCategory category = boardCategoryRepository
                                .findByIdAndDeletedAtIsNull(categoryId)
                                .orElseThrow(() -> new NotFoundException("게시판이 존재하지 않습니다."));

                if (!category.getCompany().getId().equals(companyId)) {
                        throw new ForbiddenException("해당 게시판에 대한 권한이 없습니다.");
                }

                return category;
        }

        // BoardsService (또는 BoardService) 안의 카테고리 접근 검증 로직 예시

        private void validateCategoryAccess(BoardCategory category, Long employeeId) {
                // 1) 활성화/삭제 체크 (공용/조직 공통)
                if (!category.isActive()) {
                        throw new ForbiddenException("비활성화된 게시판입니다.");
                }

                // 2) 조직 게시판이면: (기존 정책대로) permission 체크 또는 org 소속 체크
                if (category.getOrganization() != null) {
                        boolean hasPermission = boardPermissionRepository
                                        .findByEmployee_IdAndBoardCategory_Id(employeeId, category.getId())
                                        .isPresent();
                        if (!hasPermission) {
                                throw new ForbiddenException("해당 게시판 접근 권한이 없습니다.");
                        }
                        return;
                }

                // 3) 공용 게시판이면(A안):
                // - 해당 게시판에 permission이 하나도 없으면 공개
                // - permission이 하나라도 있으면, 권한 있는 사람만 접근 가능
                boolean hasAnyPermission = boardPermissionRepository.existsByBoardCategory_Id(category.getId());
                if (!hasAnyPermission)
                        return; // 공개

                boolean hasPermission = boardPermissionRepository
                                .findByEmployee_IdAndBoardCategory_Id(employeeId, category.getId())
                                .isPresent();
                if (!hasPermission) {
                        throw new ForbiddenException("해당 게시판 접근 권한이 없습니다.");
                }
        }
}