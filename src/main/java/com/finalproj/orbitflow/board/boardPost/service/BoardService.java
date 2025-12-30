package com.finalproj.orbitflow.board.boardPost.service;

import com.finalproj.orbitflow.board.boardCategory.entity.BoardCategory;
import com.finalproj.orbitflow.board.boardCategory.repository.BoardCategoryRepository;
import com.finalproj.orbitflow.board.boardPost.dto.BoardReqDto;
import com.finalproj.orbitflow.board.boardPost.dto.BoardResDto;
import com.finalproj.orbitflow.board.boardPost.entity.Board;
import com.finalproj.orbitflow.board.boardPost.repository.BoardRepository;
import com.finalproj.orbitflow.board.boardPost.repository.BoardSpecifications;
import com.finalproj.orbitflow.board.enums.BoardSearchType;
import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeRole;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardCategoryRepository boardCategoryRepository;
    private final EmployeeRepository employeeRepository;

    /** [사용자용] 게시글 목록 조회 (공용/조직 게시판 공용, 검색 포함) */
    public Page<BoardResDto.ListInfo> getBoardList(
            Long companyId,
            Long organizationId,
            Long categoryId,
            EmployeeRole role,
            LocalDate startDate,
            LocalDate endDate,
            String searchTypeStr,
            String keyword,
            Pageable pageable
    ) {
        // 1) 카테고리 접근 검증 + 조회
        BoardCategory category = getVerifiedAccessibleCategory(companyId, organizationId, categoryId, role);

        // 2) searchType 파싱
        BoardSearchType searchType;
        try {
            searchType = (searchTypeStr == null || searchTypeStr.isBlank())
                    ? BoardSearchType.ALL
                    : BoardSearchType.from(searchTypeStr);
        } catch (Exception e) {
            searchType = BoardSearchType.ALL;
        }

        // 3) 기간 조건(LocalDate -> Instant 범위)
        ZoneId zoneId = ZoneId.systemDefault();
        Instant startInstant = (startDate != null) ? startDate.atStartOfDay(zoneId).toInstant() : null;
        Instant endExclusiveInstant = (endDate != null) ? endDate.plusDays(1).atStartOfDay(zoneId).toInstant() : null;

        // 4) Spec 생성
        Specification<Board> spec = BoardSpecifications.listSpec(
                category.getId(),
                startInstant,
                endExclusiveInstant,
                searchType,
                keyword
        );

        // 5) 조회
        return boardRepository.findAll(spec, pageable)
                .map(BoardResDto.ListInfo::from);
    }

    /** [사용자용] 게시글 상세 조회 */
    @Transactional
    public BoardResDto.DetailInfo getBoardDetail(Long companyId, Long organizationId, Long boardId, EmployeeRole role) {
        Board board = boardRepository.findByIdAndDeletedAtIsNull(boardId)
                .orElseThrow(() -> new NotFoundException("게시글이 존재하지 않습니다."));

        BoardCategory category = board.getCategory();

        validateCategoryAccess(companyId, organizationId, category, role);

        board.increaseViewCount();
        return BoardResDto.DetailInfo.from(board);
    }

    /** [사용자용] 게시글 생성(공용/조직 게시판, 첨부파일 포함) */
    @Transactional
    public BoardResDto.DetailInfo createBoard(
            Long companyId,
            Long organizationId,
            Long employeeId,
            BoardReqDto.Create request,
            List<MultipartFile> files
    ) {
        BoardCategory category = getVerifiedAccessibleCategory(
                companyId,
                organizationId,
                request.getCategoryId(),
                null  // 생성 시에는 role 체크 불필요 (작성 권한은 별도 체크)
        );

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("작성자 정보가 존재하지 않습니다."));

        if (!employee.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException("게시글 작성 권한이 없습니다.");
        }

        // 파일처리: 지금은 예시로만 (네 프로젝트 파일 저장 로직에 맞춰 교체 권장)
        List<File> attachedFiles = null;
        if (files != null && !files.isEmpty()) {
            attachedFiles = files.stream()
                    .map(f -> File.builder()
                            .originFile(f.getOriginalFilename())
                            .sysFile(saveFileToSystem(f))
                            .build())
                    .toList();
        }

        Board board = Board.builder()
                .category(category)
                .writer(employee)
                .boardTitle(request.getBoardTitle())
                .boardContent(request.getBoardContent())
                .files(attachedFiles)
                .build();

        Board saved = boardRepository.save(board);
        return BoardResDto.DetailInfo.from(saved);
    }

    /** 게시판 카테고리 접근 검증 + 조회 */
    private BoardCategory getVerifiedAccessibleCategory(Long companyId, Long organizationId, Long categoryId, EmployeeRole role) {
        BoardCategory category = boardCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("게시판 카테고리를 찾을 수 없습니다."));

        validateCategoryAccess(companyId, organizationId, category, role);
        return category;
    }

    /** 게시판 접근 가능 여부 검증 */
    private void validateCategoryAccess(Long companyId, Long organizationId, BoardCategory category, EmployeeRole role) {
        if (!category.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException("접근 권한이 없는 게시판입니다.");
        }
        if (!category.isActivated()) {
            throw new ForbiddenException("비활성화된 게시판입니다.");
        }

        // 공용 게시판(organization이 null)이고 ADMIN 권한이 있으면 권한 체크 건너뛰기
        boolean isAdmin = (role == EmployeeRole.ADMIN || role == EmployeeRole.COMPANY_ADMIN);
        if (category.getOrganization() == null && isAdmin) {
            // ADMIN은 공용 게시판에 대해 권한 체크 없이 접근 가능
            return;
        }

        // 조직 게시판이면 organizationId 일치해야 함
        if (category.getOrganization() != null) {
            if (organizationId == null || !category.getOrganization().getId().equals(organizationId)) {
                throw new ForbiddenException("소속 조직 게시판이 아닙니다.");
            }
        }
    }

    /** 실제 파일 시스템 저장 (예시) */
    private String saveFileToSystem(MultipartFile file) {
        return "/files/" + file.getOriginalFilename();
    }

    @Transactional
    public BoardResDto.DetailInfo updateBoard(
            Long companyId,
            Long organizationId,
            Long employeeId,
            Long boardId,
            BoardReqDto.Update request,
            List<MultipartFile> files
    ) {
        Board board = boardRepository.findByIdAndDeletedAtIsNull(boardId)
                .orElseThrow(() -> new NotFoundException("게시글이 존재하지 않습니다."));

        BoardCategory category = board.getCategory();
        validateCategoryAccess(companyId, organizationId, category, null);  // 수정 시에는 role 체크 불필요

        // 작성자 본인 또는 관리자만 허용(관리자 정책은 네 role에 맞게 조정)
        boolean isWriter = board.getWriter().getId().equals(employeeId);
        if (!isWriter) {
            throw new ForbiddenException("게시글 수정 권한이 없습니다.");
        }

        // 파일 처리(선택) - createBoard와 동일하게 저장 로직을 태우면 됨
        List<File> attachedFiles = null;
        if (files != null && !files.isEmpty()) {
            attachedFiles = files.stream()
                    .map(file -> File.builder()
                            .originFile(file.getOriginalFilename())
                            .sysFile(saveFileToSystem(file))
                            .build())
                    .toList();
        }

        board.update(request.getBoardTitle(), request.getBoardContent(), attachedFiles);

        // Dirty checking으로 반영되므로 save() 없어도 됨.
        return BoardResDto.DetailInfo.from(board);
    }

    @Transactional
    public void deleteBoard(
            Long companyId,
            Long organizationId,
            Long employeeId,
            Long boardId
    ) {
        Board board = boardRepository.findByIdAndDeletedAtIsNull(boardId)
                .orElseThrow(() -> new NotFoundException("게시글이 존재하지 않습니다."));

        BoardCategory category = board.getCategory();
        validateCategoryAccess(companyId, organizationId, category, null);  // 삭제 시에는 role 체크 불필요

        boolean isWriter = board.getWriter().getId().equals(employeeId);
        if (!isWriter) {
            throw new ForbiddenException("게시글 삭제 권한이 없습니다.");
        }

        board.softDelete(); // soft delete
    }
}
