package com.finalproj.orbitflow.board.boardPost.service;

import com.finalproj.orbitflow.board.boardCategory.entity.BoardCategory;
import com.finalproj.orbitflow.board.boardCategory.repository.BoardCategoryRepository;
import com.finalproj.orbitflow.board.boardPost.dto.BoardResDto;
import com.finalproj.orbitflow.board.boardPost.entity.Board;
import com.finalproj.orbitflow.board.boardPost.repository.BoardRepository;
import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardCategoryRepository boardCategoryRepository;

    /** [사용자용] 게시글 목록 조회 (공용 / 조직 게시판 공용) */
    public Page<BoardResDto.ListInfo> getBoardList(
            Long companyId,
            Long organizationId,
            Long categoryId,
            Pageable pageable
    ) {
        BoardCategory category = getVerifiedAccessibleCategory(
                companyId,
                organizationId,
                categoryId
        );

        Page<Board> page =
                boardRepository.findAllByCategory_IdAndDeletedAtIsNull(
                        category.getId(),
                        pageable
                );

        return page.map(BoardResDto.ListInfo::from);
    }

    /** 게시판 접근 가능 여부 검증 */
    private BoardCategory getVerifiedAccessibleCategory(
            Long companyId,
            Long organizationId,
            Long categoryId
    ) {
        BoardCategory category = boardCategoryRepository
                .findByIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> new NotFoundException("게시판이 존재하지 않습니다."));

        if (!category.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException("접근 권한이 없는 게시판입니다.");
        }

        if (!category.isActivated()) {
            throw new ForbiddenException("비활성화된 게시판입니다.");
        }

        if (category.getOrganization() != null) {
            if (organizationId == null ||
                    !category.getOrganization().getId().equals(organizationId)) {
                throw new ForbiddenException("소속 조직 게시판이 아닙니다.");
            }
        }

        return category;
    }

    /** 게시글 상세 조회 */
    @Transactional
    public BoardResDto.DetailInfo getBoardDetail(
            Long companyId,
            Long organizationId,
            Long boardId
    ) {
        Board board = boardRepository.findByIdAndDeletedAtIsNull(boardId)
                .orElseThrow(() -> new NotFoundException("게시글이 존재하지 않습니다."));

        BoardCategory category = board.getCategory();

        // 1. 회사 검증
        if (!category.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException("접근 권한이 없는 게시글입니다.");
        }

        // 2. 게시판 활성화 여부
        if (!category.isActivated()) {
            throw new ForbiddenException("비활성화된 게시판입니다.");
        }

        // 3. 조직 게시판 접근 검증
        if (category.getOrganization() != null) {
            if (organizationId == null ||
                    !category.getOrganization().getId().equals(organizationId)) {
                throw new ForbiddenException("소속 조직 게시판이 아닙니다.");
            }
        }

        // 4. 조회수 증가
        board.increaseViewCount();

        return BoardResDto.DetailInfo.from(board);
    }
}
