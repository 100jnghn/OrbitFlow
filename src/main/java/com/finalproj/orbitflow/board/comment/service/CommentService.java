package com.finalproj.orbitflow.board.comment.service;

import com.finalproj.orbitflow.board.boardCategory.entity.BoardCategory;
import com.finalproj.orbitflow.board.comment.dto.CommentResDto;
import com.finalproj.orbitflow.board.boardPost.entity.Board;
import com.finalproj.orbitflow.board.boardPost.repository.BoardRepository;
import com.finalproj.orbitflow.board.comment.repository.CommentRepository;
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
public class CommentService {
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    public Page<CommentResDto.ListInfo> getCommentList(
            Long companyId,
            Long organizationId,
            Long boardId,
            Pageable pageable
    ) {
        // 1) 게시글 존재 + soft delete 검증
        Board board = boardRepository.findByIdAndDeletedAtIsNull(boardId)
                .orElseThrow(() -> new NotFoundException("게시글이 존재하지 않습니다."));

        // 2) 게시판 접근 검증 (회사/활성화/조직)
        validateBoardAccess(companyId, organizationId, board);

        // 3) 댓글 조회 (deletedAt null만)
        return commentRepository.findByBoardIdAndDeletedAtIsNull(boardId, pageable)
                .map(CommentResDto.ListInfo::from);
    }

    private void validateBoardAccess(Long companyId, Long organizationId, Board board) {
        BoardCategory category = board.getCategory();

        if (!category.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException("접근 권한이 없는 게시글입니다.");
        }
        if (!category.isActivated()) {
            throw new ForbiddenException("비활성화된 게시판입니다.");
        }
        if (category.getOrganization() != null) {
            if (organizationId == null || !category.getOrganization().getId().equals(organizationId)) {
                throw new ForbiddenException("소속 조직 게시판이 아닙니다.");
            }
        }
    }
}
