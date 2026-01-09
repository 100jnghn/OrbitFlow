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
import com.finalproj.orbitflow.global.file.enums.FileDomain;
import com.finalproj.orbitflow.global.file.service.FileService;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeRole;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.organization.repository.OrgRepository;
import com.finalproj.orbitflow.hr.organization.repository.OrgResView;
import com.finalproj.orbitflow.notification.enums.NotificationType;
import com.finalproj.orbitflow.notification.service.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardCategoryRepository boardCategoryRepository;
    private final EmployeeRepository employeeRepository;
    private final FileService fileService;
    private final OrgRepository orgRepository;
    private final S3Client s3Client;
    private final NotificationCommandService notificationCommandService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

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
            Pageable pageable) {
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
                keyword);

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
            List<MultipartFile> files) {
        BoardCategory category = getVerifiedAccessibleCategory(
                companyId,
                organizationId,
                request.getCategoryId(),
                null // 생성 시에는 role 체크 불필요 (작성 권한은 별도 체크)
        );

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("작성자 정보가 존재하지 않습니다."));

        if (!employee.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException("게시글 작성 권한이 없습니다.");
        }

        // 파일 크기 검증
        validateFileSize(files);

        // 파일 처리: FileService를 사용하여 S3에 업로드
        List<File> attachedFiles = null;
        if (files != null && !files.isEmpty()) {
            attachedFiles = files.stream()
                    .map(file -> fileService.upload(companyId, FileDomain.BOARD, file))
                    .toList();
        }

        Board board = Board.builder()
                .category(category)
                .writer(employee)
                .boardTitle(request.getBoardTitle())
                .boardContent(request.getBoardContent())
                .files(attachedFiles)
                .build();

        com.finalproj.orbitflow.board.boardPost.entity.Board saved = boardRepository.save(board);

        // 공지사항(NOTICE) 타입 게시글 작성 시 전 사원에게 알림 생성
        if (com.finalproj.orbitflow.board.boardCategory.enums.Board.NOTICE.name().equals(category.getBoardType())) {
            List<Employee> allActiveEmployees = employeeRepository.findByCompanyIdAndStatus(
                    companyId, EmployeeStatus.ACTIVE);

            String notificationMessage = String.format("새로운 공지사항이 등록되었습니다.\n제목: %s", saved.getBoardTitle());

            for (Employee emp : allActiveEmployees) {
                // 작성자 본인 제외
                if (emp.getId().equals(employeeId))
                    continue;

                notificationCommandService.createNotification(
                        companyId,
                        emp.getId(),
                        NotificationType.BOARD,
                        notificationMessage,
                        "/view/board/detail?boardId=" + saved.getId());
            }
        }

        return BoardResDto.DetailInfo.from(saved);
    }

    /** 게시판 카테고리 접근 검증 + 조회 */
    private BoardCategory getVerifiedAccessibleCategory(Long companyId, Long organizationId, Long categoryId,
            EmployeeRole role) {
        BoardCategory category = boardCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("게시판 카테고리를 찾을 수 없습니다."));

        validateCategoryAccess(companyId, organizationId, category, role);
        return category;
    }

    /** 게시판 접근 가능 여부 검증 */
    private void validateCategoryAccess(Long companyId, Long organizationId, BoardCategory category,
            EmployeeRole role) {
        if (!category.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException("접근 권한이 없는 게시판입니다.");
        }
        if (category.getDeletedAt() != null) {
            throw new ForbiddenException("삭제된 게시판입니다.");
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

        // 조직 게시판이면 소속 조직 또는 상위 조직 계층에 포함되어야 함
        if (category.getOrganization() != null) {
            if (organizationId == null) {
                throw new ForbiddenException("조직 정보가 없습니다.");
            }

            // 사용자의 조직 계층 구조 조회
            List<OrgResView> hierarchy = orgRepository.findHierarchy(organizationId);
            boolean isAccessible = hierarchy.stream()
                    .anyMatch(view -> view.getId().equals(category.getOrganization().getId()));

            if (!isAccessible) {
                throw new ForbiddenException("소속 또는 상위 조직 게시판이 아닙니다.");
            }
        }
    }

    @Transactional
    public BoardResDto.DetailInfo updateBoard(
            Long companyId,
            Long organizationId,
            Long employeeId,
            Long boardId,
            BoardReqDto.Update request,
            List<MultipartFile> files) {
        Board board = boardRepository.findByIdAndDeletedAtIsNull(boardId)
                .orElseThrow(() -> new NotFoundException("게시글이 존재하지 않습니다."));

        BoardCategory category = board.getCategory();
        validateCategoryAccess(companyId, organizationId, category, null); // 수정 시에는 role 체크 불필요

        // 작성자 본인 또는 관리자만 허용(관리자 정책은 네 role에 맞게 조정)
        boolean isWriter = board.getWriter().getId().equals(employeeId);
        if (!isWriter) {
            throw new ForbiddenException("게시글 수정 권한이 없습니다.");
        }

        // 파일 크기 검증
        validateFileSize(files);

        // 기존 파일 정보 저장 (S3 삭제용)
        List<File> existingFiles = board.getFiles() != null ? new java.util.ArrayList<>(board.getFiles()) : null;

        // 새 파일 업로드
        List<File> attachedFiles = null;
        if (files != null && !files.isEmpty()) {
            attachedFiles = files.stream()
                    .map(file -> fileService.upload(companyId, FileDomain.BOARD, file))
                    .toList();
        }

        // 게시글 업데이트 (파일 교체)
        board.update(request.getBoardTitle(), request.getBoardContent(), attachedFiles);

        // 기존 파일 삭제 (S3에서도 삭제) - 업데이트 후에 삭제
        if (existingFiles != null && !existingFiles.isEmpty()) {
            existingFiles.forEach(file -> {
                if (file.getObjectKey() != null) {
                    deleteObject(file.getObjectKey());
                }
            });
        }

        // Dirty checking으로 반영되므로 save() 없어도 됨.
        return BoardResDto.DetailInfo.from(board);
    }

    @Transactional
    public void deleteBoard(
            Long companyId,
            Long organizationId,
            Long employeeId,
            Long boardId) {
        Board board = boardRepository.findByIdAndDeletedAtIsNull(boardId)
                .orElseThrow(() -> new NotFoundException("게시글이 존재하지 않습니다."));

        BoardCategory category = board.getCategory();
        validateCategoryAccess(companyId, organizationId, category, null); // 삭제 시에는 role 체크 불필요

        boolean isWriter = board.getWriter().getId().equals(employeeId);
        if (!isWriter) {
            throw new ForbiddenException("게시글 삭제 권한이 없습니다.");
        }

        // 첨부된 파일 삭제 (S3에서도 삭제)
        if (board.getFiles() != null && !board.getFiles().isEmpty()) {
            board.getFiles().forEach(file -> {
                if (file.getObjectKey() != null) {
                    deleteObject(file.getObjectKey());
                }
            });
        }

        board.softDelete(); // soft delete
    }

    /** 파일 크기 검증 (50MB 제한) */
    private void validateFileSize(List<MultipartFile> files) {
        if (files != null) {
            long maxSize = 50 * 1024 * 1024; // 50MB
            for (MultipartFile file : files) {
                if (file.getSize() > maxSize) {
                    throw new com.finalproj.orbitflow.global.exception.InvalidRequestException(
                            "파일 크기는 50MB를 초과할 수 없습니다: " + file.getOriginalFilename());
                }
            }
        }
    }

    /** S3에서 파일 삭제 */
    private void deleteObject(String objectKey) {
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .build());
        } catch (Exception ex) {
            log.error("S3 delete failed. objectKey={}", objectKey, ex);
        }
    }
}
