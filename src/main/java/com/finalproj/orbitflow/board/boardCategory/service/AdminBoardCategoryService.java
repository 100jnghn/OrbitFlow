package com.finalproj.orbitflow.board.boardCategory.service;

import com.finalproj.orbitflow.board.boardCategory.dto.BoardCategoryReqDto;
import com.finalproj.orbitflow.board.boardCategory.dto.BoardCategoryResDto;
import com.finalproj.orbitflow.board.boardCategory.entity.BoardCategory;
import com.finalproj.orbitflow.board.boardCategory.repository.BoardCategoryRepository;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminBoardCategoryService {

    private final BoardCategoryRepository boardCategoryRepository;
    private final CompanyRepository companyRepository;
    private final OrganizationRepository organizationRepository;

    /**
     * 관리자 게시판(카테고리) 목록 조회
     * - 삭제되지 않은 카테고리만 조회
     * - 활성/비활성 여부 상관없이 조회
     */
    public Page<BoardCategoryResDto.Category> getCategoryList(Long companyId, Pageable pageable) {

        Page<BoardCategory> categoryPage =
                boardCategoryRepository.findAllByCompany_Id(companyId, pageable);

        return categoryPage.map(BoardCategoryResDto.Category::from);
    }


    /**
     * 관리자 게시판 카테고리 단건 조회
     * - 수정 페이지 로딩용
     * - 비활성화된 조직 게시판도 조회 가능
     */
    public BoardCategoryResDto.Category getCategoryDetail(Long categoryId) {

        BoardCategory category = boardCategoryRepository
                .findByIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 게시판 카테고리입니다. id=" + categoryId)
                );

        return BoardCategoryResDto.Category.from(category);
    }

    /**
     * 게시판 카테고리 생성
     */
    @Transactional
    public Long createCategory(BoardCategoryReqDto.Create dto) {

        BoardCategory category = BoardCategory.builder()
                .company(companyRepository.getReferenceById(dto.getCompanyId()))
                .organization(
                        dto.getOrganizationId() != null
                                ? organizationRepository.getReferenceById(dto.getOrganizationId())
                                : null
                )
                .boardName(dto.getBoardName())
                .boardType(dto.getBoardType())
                .isActivated(true) // 일반 게시판은 항상 활성
                .commentActivated(dto.getCommentActivated())
                .build();

        return boardCategoryRepository.save(category).getId();
    }

    /**
     * 게시판 카테고리 수정
     */
    @Transactional
    public void updateCategory(Long categoryId, BoardCategoryReqDto.Update dto) {

        BoardCategory category = boardCategoryRepository
                .findByIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판"));

        category.update(
                dto.getBoardName(),
                dto.getBoardType(),
                // 일반 게시판이면 무시
                category.getOrganization() == null ? true : dto.getIsActivated(),
                dto.getCommentActivated()
        );
    }


    /**
     * 게시판 카테고리 삭제 (소프트 삭제)
     */
    @Transactional
    public void deleteCategory(Long categoryId) {

        BoardCategory category = boardCategoryRepository.findByIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("게시판이 존재하지 않습니다."));

        // 공지사항은 삭제 불가
        if ("NOTICE".equals(category.getBoardType())) {
            throw new IllegalStateException("공지사항 게시판은 삭제할 수 없습니다.");
        }

        category.softDelete();
    }
}
