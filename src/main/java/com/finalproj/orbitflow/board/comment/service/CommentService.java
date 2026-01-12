package com.finalproj.orbitflow.board.comment.service;

import com.finalproj.orbitflow.board.boardCategory.entity.BoardCategory;
import com.finalproj.orbitflow.board.boardPost.entity.BoardPost;
import com.finalproj.orbitflow.board.boardPost.repository.BoardPostRepository;
import com.finalproj.orbitflow.board.comment.dto.CommentReqDto;
import com.finalproj.orbitflow.board.comment.dto.CommentResDto;
import com.finalproj.orbitflow.board.comment.entity.Comment;
import com.finalproj.orbitflow.board.comment.repository.CommentRepository;
import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.notification.enums.NotificationType;
import com.finalproj.orbitflow.notification.service.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final BoardPostRepository boardPostRepository;
    private final CommentRepository commentRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationCommandService notificationCommandService;

    /** 댓글 목록 조회 */
    public Page<CommentResDto.ListInfo> getCommentList(
            Long companyId,
            Long organizationId,
            Long boardId,
            Pageable pageable) {
        // 1) 게시글 존재 + soft delete 검증
        BoardPost boardPost = boardPostRepository.findByIdAndDeletedAtIsNull(boardId)
                .orElseThrow(() -> new NotFoundException("게시글이 존재하지 않습니다."));

        // 2) 게시판 접근 검증 (회사/활성화/조직)
        validateBoardAccess(companyId, organizationId, boardPost);

        // 3) 댓글 조회 (deletedAt null만)
        return commentRepository.findByBoardPostIdAndDeletedAtIsNull(boardId, pageable)
                .map(CommentResDto.ListInfo::from);
    }

    /** 게시판 접근 검증 */
    private void validateBoardAccess(Long companyId, Long organizationId, BoardPost boardPost) {
        BoardCategory category = boardPost.getCategory();

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

    /** 댓글 작성 */
    @Transactional
    public CommentResDto.DetailInfo createComment(
            Long companyId,
            Long organizationId,
            Long employeeId,
            Long boardId,
            CommentReqDto.Create request) {
        BoardPost boardPost = boardPostRepository.findByIdAndDeletedAtIsNull(boardId)
                .orElseThrow(() -> new NotFoundException("게시글이 존재하지 않습니다."));

        // 게시판 접근 검증 (기존 BoardService 로직과 동일 개념)
        validateBoardAccess(companyId, organizationId, boardPost);

        Employee writer = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("작성자 정보가 없습니다."));

        Comment comment = Comment.builder()
                .boardPost(boardPost)
                .writer(writer)
                .commentContent(request.getCommentContent())
                .build();

        Comment savedComment = commentRepository.save(comment);

        // 게시글 작성자에게 알림 전송 (본인이 작성한 댓글 제외)
        Employee boardWriter = boardPost.getWriter();
        if (!boardWriter.getId().equals(employeeId)) {
            String contentSample = savedComment.getCommentContent();
            if (contentSample.length() > 100) {
                contentSample = contentSample.substring(0, 100) + "...";
            }

            String notificationMessage = String.format("작성하신 게시물에 새로운 댓글이 달렸습니다.\n내용: %s",
                    contentSample);

            notificationCommandService.createNotification(
                    companyId,
                    boardWriter.getId(),
                    NotificationType.BOARD,
                    notificationMessage,
                    "/view/board/detail?boardId=" + boardId);
        }

        return CommentResDto.DetailInfo.from(savedComment);
    }

    /** 댓글 수정 */
    @Transactional
    public CommentResDto.DetailInfo updateComment(
            Long companyId,
            Long employeeId,
            Long commentId,
            CommentReqDto.Update request) {
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new NotFoundException("댓글이 존재하지 않습니다."));

        // 회사 검증
        if (!comment.getBoardPost().getCategory().getCompany().getId().equals(companyId)) {
            throw new ForbiddenException("수정 권한이 없습니다.");
        }

        // 작성자 검증
        if (!comment.getWriter().getId().equals(employeeId)) {
            throw new ForbiddenException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        comment.updateContent(request.getCommentContent());

        return CommentResDto.DetailInfo.from(comment);
    }

    /** 댓글 삭제 */
    @Transactional
    public void deleteComment(
            Long companyId,
            Long employeeId,
            Long commentId) {
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new NotFoundException("댓글이 존재하지 않습니다."));

        if (!comment.getBoardPost().getCategory().getCompany().getId().equals(companyId)) {
            throw new ForbiddenException("삭제 권한이 없습니다.");
        }

        if (!comment.getWriter().getId().equals(employeeId)) {
            throw new ForbiddenException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        comment.softDelete(); // deletedAt 처리
    }
}
