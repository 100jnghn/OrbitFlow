package com.finalproj.orbitflow.message.service;

import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.global.file.enums.FileDomain;
import com.finalproj.orbitflow.global.file.repository.FileRepository;
import com.finalproj.orbitflow.global.file.service.FileService;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import com.finalproj.orbitflow.message.dto.MessageReqDto;
import com.finalproj.orbitflow.message.dto.MessageResDto;
import com.finalproj.orbitflow.message.entity.Message;
import com.finalproj.orbitflow.message.entity.MessageRecipient;
import com.finalproj.orbitflow.message.enums.MessageFolderType;
import com.finalproj.orbitflow.message.enums.MessageSearchType;
import com.finalproj.orbitflow.message.repository.MessageRecipientRepository;
import com.finalproj.orbitflow.message.repository.MessageRecipientSpecifications;
import com.finalproj.orbitflow.message.repository.MessageRepository;
import com.finalproj.orbitflow.notification.enums.NotificationType;
import com.finalproj.orbitflow.notification.service.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageRecipientRepository messageRecipientRepository;
    private final EmployeeRepository employeeRepository;
    private final FileService fileService;
    private final FileRepository fileRepository;
    private final S3Client s3Client;
    private final NotificationCommandService notificationCommandService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /** 메시지함 목록 */
    public Page<MessageResDto.ListItem> getMessageList(
            Long companyId,
            Long employeeId,
            MessageFolderType folder,
            boolean archived,
            LocalDate startDate,
            LocalDate endDate,
            String searchTypeStr,
            String keyword,
            Pageable pageable) {
        // searchType 파싱
        MessageSearchType searchType = MessageSearchType.from(searchTypeStr);

        // 기간 조건 변환 (LocalDate -> Instant)
        ZoneId zoneId = ZoneId.systemDefault();
        Instant startInstant = (startDate != null) ? startDate.atStartOfDay(zoneId).toInstant() : null;
        Instant endExclusiveInstant = (endDate != null) ? endDate.plusDays(1).atStartOfDay(zoneId).toInstant() : null;

        // 검색어나 기간 조건이 있으면 Specification 사용
        boolean hasSearch = (keyword != null && !keyword.isBlank());
        boolean hasDateFilter = (startDate != null || endDate != null);

        Page<MessageRecipient> page;
        if (archived) {
            if (hasSearch || hasDateFilter) {
                Specification<MessageRecipient> spec = MessageRecipientSpecifications.archiveSpec(
                        companyId, employeeId, startInstant, endExclusiveInstant, searchType, keyword);
                // 정렬을 pageable에 포함
                pageable = org.springframework.data.domain.PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt"));
                page = messageRecipientRepository.findAll(spec, pageable);
            } else {
                page = messageRecipientRepository
                        .findByCompanyIdAndEmployee_IdAndDeletedAtIsNullAndIsArchivedTrueOrderByCreatedAtDesc(
                                companyId, employeeId, pageable);
            }
        } else {
            // 보낸메시지함(SENT)은 검색어가 없더라도 수신자 단위 조회를 위해 Specification 사용이 필수적임
            if (hasSearch || hasDateFilter || folder == MessageFolderType.SENT) {
                Specification<MessageRecipient> spec = MessageRecipientSpecifications.listSpec(
                        companyId, employeeId, folder, startInstant, endExclusiveInstant, searchType, keyword);
                // 정렬을 pageable에 포함
                pageable = org.springframework.data.domain.PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt"));
                page = messageRecipientRepository.findAll(spec, pageable);
            } else {
                page = messageRecipientRepository
                        .findByCompanyIdAndEmployee_IdAndDeletedAtIsNullAndIsArchivedFalseAndMessageFolderTypeOrderByCreatedAtDesc(
                                companyId, employeeId, folder, pageable);
            }
        }

        // SENT 폴더인 경우: Specification에서 이미 INBOX 레코드(수신자별 행)를 조회하도록 구조가 변경됨
        // 따라서 이전의 수동 확장 및 페이징 로직은 더 이상 필요 없음
        if (!archived && folder == MessageFolderType.SENT) {
            return page.map(mr -> {
                String peerName = mr.getEmployee().getName();
                return MessageResDto.ListItem.builder()
                        .messageId(mr.getMessage().getId())
                        .recipientId(mr.getId())
                        .folderType(MessageFolderType.SENT)
                        .archived(mr.isArchived())
                        .read(mr.isRead())
                        .readAt(mr.getReadAt())
                        .title(mr.getMessage().getMessageTitle())
                        .peerName(peerName)
                        .senderName(mr.getMessage().getSender().getName())
                        .recipientName(null)
                        .hasFile(mr.getMessage().getFiles() != null && !mr.getMessage().getFiles().isEmpty())
                        .createdAt(mr.getMessage().getCreatedAt())
                        .build();
            });
        }

        // INBOX, ARCHIVE: 기존 로직 유지
        // peerName 정책:
        // - INBOX: senderName
        // - ARCHIVE: 기존 로직
        return page.map(mr -> {
            String peerName;
            String recipientName = null;

            if (mr.getMessageFolderType() == MessageFolderType.INBOX) {
                peerName = mr.getMessage().getSender().getName();
                // 보관함에서 받은 메시지인 경우, 현재 사용자가 수신자
                if (archived) {
                    recipientName = mr.getEmployee().getName();
                }
            } else {
                // ARCHIVE에서 SENT 타입인 경우: 수신자 이름 사용
                List<MessageRecipient> recipients = messageRecipientRepository
                        .findByMessage_IdAndMessageFolderTypeAndDeletedAtIsNull(
                                mr.getMessage().getId(), MessageFolderType.INBOX);
                if (!recipients.isEmpty()) {
                    String firstRecipient = recipients.get(0).getEmployee().getName();
                    if (recipients.size() > 1) {
                        peerName = firstRecipient + " 외 " + (recipients.size() - 1) + "명";
                    } else {
                        peerName = firstRecipient;
                    }
                    // 보관함에서 보낸 메시지인 경우, recipientName도 동일하게 설정
                    if (archived) {
                        recipientName = peerName;
                    }
                } else {
                    peerName = "수신자 없음";
                    if (archived) {
                        recipientName = peerName;
                    }
                }
            }

            MessageResDto.ListItem item = MessageResDto.ListItem.from(mr, peerName);
            // 보관함인 경우 recipientName 설정
            if (archived && recipientName != null) {
                // Builder 패턴이므로 새로운 객체 생성 필요
                return MessageResDto.ListItem.builder()
                        .messageId(item.getMessageId())
                        .recipientId(item.getRecipientId())
                        .folderType(item.getFolderType())
                        .archived(item.isArchived())
                        .read(item.isRead())
                        .readAt(item.getReadAt())
                        .title(item.getTitle())
                        .peerName(item.getPeerName())
                        .senderName(item.getSenderName())
                        .recipientName(recipientName)
                        .hasFile(item.isHasFile())
                        .createdAt(item.getCreatedAt())
                        .build();
            }
            return item;
        });
    }

    /** 메시지 상세 조회 (+ 받은 편지함이면 읽음 처리) */
    @Transactional
    public MessageResDto.Detail getMessageDetail(
            Long companyId,
            Long employeeId,
            Long messageId,
            Long recipientId // 보낸 메시지함에서 특정 수신자 선택 시 사용 (optional)
    ) {
        // 1. 현재 사용자의 해당 메시지 레코드 조회 (자신이 삭제했는지 최우선 확인)
        MessageRecipient currentUserRecord = messageRecipientRepository
                .findByCompanyIdAndMessage_IdAndEmployee_IdAndDeletedAtIsNull(companyId, messageId, employeeId)
                .orElseThrow(() -> new NotFoundException("메시지가 존재하지 않습니다."));

        MessageRecipient mr;

        // 2. recipientId가 제공된 경우 (보낸 메시지함에서 특정 수신자 선택 시)
        if (recipientId != null) {
            mr = messageRecipientRepository.findById(recipientId)
                    .filter(r -> r.getDeletedAt() == null) // 수신자 레코드도 삭제되지 않았어야 함
                    .orElseThrow(() -> new NotFoundException("상대방의 수신 정보를 찾을 수 없습니다."));

            // 권한 및 무결성 확인
            if (!mr.getCompanyId().equals(companyId) || !mr.getMessage().getId().equals(messageId)) {
                throw new ForbiddenException("메시지 정보가 일치하지 않습니다.");
            }
            // 보낸 사람 본인인지 확인
            if (!mr.getMessage().getSender().getId().equals(employeeId)) {
                throw new ForbiddenException("메시지 수신 정보를 볼 권한이 없습니다.");
            }
        } else {
            mr = currentUserRecord;
        }

        MessageResDto.Detail detail = MessageResDto.Detail.from(mr);

        // 보낸 메시지함인 경우 수신자 정보 추가
        // recipientId가 제공된 경우 (보낸 메시지함에서 특정 수신자 선택)
        // 또는 보관함에서 보낸 메시지(SENT 타입)인 경우
        boolean isSentMessageDetail = recipientId != null ||
                (mr.getMessageFolderType() == MessageFolderType.INBOX &&
                        mr.getMessage().getSender().getId().equals(employeeId))
                ||
                (mr.getMessageFolderType() == MessageFolderType.SENT &&
                        mr.getMessage().getSender().getId().equals(employeeId));

        if (isSentMessageDetail) {
            // 보낸 메시지함 또는 보관함에서 보낸 메시지
            Long recipientIdDetail = null;
            String recipientName = null;

            if (recipientId != null && mr.getMessageFolderType() == MessageFolderType.INBOX) {
                // 보낸 메시지함에서 특정 수신자 선택한 경우: 해당 수신자 정보만 표시
                recipientIdDetail = mr.getEmployee().getId();
                recipientName = mr.getEmployee().getName();
            } else {
                // 보관함에서 보낸 메시지(SENT 타입)인 경우: 모든 수신자 정보 조회하여 표시
                List<MessageRecipient> inboxRecipients = messageRecipientRepository
                        .findByMessage_IdAndMessageFolderTypeAndDeletedAtIsNull(
                                mr.getMessage().getId(), MessageFolderType.INBOX);

                if (!inboxRecipients.isEmpty()) {
                    if (inboxRecipients.size() == 1) {
                        recipientIdDetail = inboxRecipients.get(0).getEmployee().getId();
                        recipientName = inboxRecipients.get(0).getEmployee().getName();
                    } else {
                        // 여러 수신자인 경우: 첫 번째 수신자 ID와 모든 수신자 이름 표시
                        recipientIdDetail = inboxRecipients.get(0).getEmployee().getId();
                        recipientName = inboxRecipients.stream()
                                .map(recipient -> recipient.getEmployee().getName())
                                .collect(java.util.stream.Collectors.joining(", "));
                    }
                }
            }

            // 읽은 일시: 수신자 중 읽은 수신자가 있으면 그 중 하나의 readAt 사용
            Instant readAt = detail.getReadAt();
            if (mr.getMessageFolderType() == MessageFolderType.SENT || recipientId == null) {
                // 보관함에서 SENT 레코드를 조회한 경우 또는 보낸 메시지함에서 모든 수신자 정보를 표시하는 경우
                List<MessageRecipient> inboxRecipients = messageRecipientRepository
                        .findByMessage_IdAndMessageFolderTypeAndDeletedAtIsNull(
                                mr.getMessage().getId(), MessageFolderType.INBOX);
                readAt = inboxRecipients.stream()
                        .filter(MessageRecipient::isRead)
                        .findFirst()
                        .map(MessageRecipient::getReadAt)
                        .orElse(null);
            }

            // 발신자가 조회하는 경우이므로 읽음 처리하지 않음 (수신자의 readAt은 그대로 유지)
            return MessageResDto.Detail.builder()
                    .messageId(detail.getMessageId())
                    .recipientId(detail.getRecipientId())
                    .title(detail.getTitle())
                    .content(detail.getContent())
                    .senderId(detail.getSenderId())
                    .senderName(detail.getSenderName())
                    .recipientIdDetail(recipientIdDetail)
                    .recipientName(recipientName)
                    .folderType(MessageFolderType.SENT) // 보낸 메시지함으로 표시
                    .archived(detail.isArchived())
                    .read(detail.isRead())
                    .readAt(readAt) // 수신자가 읽은 일시 (발신자가 조회해도 변경되지 않음)
                    .files(MessageResDto.FileInfo.fromFiles(mr.getMessage().getFiles()))
                    .createdAt(detail.getCreatedAt())
                    .build();
        }

        // 받은 편지함이면 읽음 처리 (수신자가 자신의 메시지를 조회하는 경우)
        if (mr.getMessageFolderType() == MessageFolderType.INBOX) {
            mr.markRead();
        }

        return detail;
    }

    /** 메시지 전송 */
    @Transactional
    public Long sendMessage(Long companyId, Long senderEmployeeId, MessageReqDto.Send request,
            java.util.List<MultipartFile> files) {

        // 제목 및 내용 글자수 검증
        if (request.getMessageTitle() != null && request.getMessageTitle().length() > 100) {
            throw new InvalidRequestException("제목은 100자 이하여야 합니다.");
        }

        if (request.getMessageContent() != null && request.getMessageContent().length() > 3000) {
            throw new InvalidRequestException("내용은 3,000자 이하여야 합니다.");
        }

        // 공백만 입력된 내용 검증
        if (request.getMessageContent() != null && request.getMessageContent().trim().isEmpty()) {
            throw new InvalidRequestException("공백만 입력된 내용은 전송할 수 없습니다.");
        }

        // 발신자
        Employee sender = employeeRepository.findById(senderEmployeeId)
                .orElseThrow(() -> new NotFoundException("발신자 정보를 찾을 수 없습니다."));
        if (!sender.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException("해당 회사의 사용자가 아닙니다.");
        }

        // 수신자 검증 + (중요) 본인에게 보내기 금지(uk 제약 때문)
        if (request.getRecipientEmployeeIds().stream().anyMatch(id -> id.equals(senderEmployeeId))) {
            throw new InvalidRequestException("본인에게는 메시지를 보낼 수 없습니다.");
        }

        List<Employee> recipients = employeeRepository.findAllById(request.getRecipientEmployeeIds());
        if (recipients.size() != request.getRecipientEmployeeIds().size()) {
            throw new InvalidRequestException("존재하지 않는 수신자가 포함되어 있습니다.");
        }
        for (Employee r : recipients) {
            if (!r.getCompany().getId().equals(companyId)) {
                throw new ForbiddenException("다른 회사 직원에게는 메시지를 보낼 수 없습니다.");
            }
        }

        // 파일 크기 검증
        validateFileSize(files);

        // 파일 업로드 처리
        java.util.List<File> uploadedFiles = new java.util.ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && file.getOriginalFilename() != null && !file.getOriginalFilename().isBlank()) {
                    uploadedFiles.add(fileService.upload(companyId, FileDomain.MESSAGE, file));
                }
            }
        }

        if (request.getFileIds() != null && !request.getFileIds().isEmpty()) {
            uploadedFiles.addAll(fileRepository.findAllById(request.getFileIds()));
        }

        // Message(본문) 생성
        Message message = Message.builder()
                .companyId(companyId)
                .sender(sender)
                .messageTitle(request.getMessageTitle())
                .messageContent(request.getMessageContent())
                .files(uploadedFiles)
                .build();

        Message saved = messageRepository.save(message);

        // 수신자 INBOX row 생성
        List<MessageRecipient> recipientRows = recipients.stream()
                .map(r -> MessageRecipient.builder()
                        .companyId(companyId)
                        .message(saved)
                        .employee(r)
                        .messageFolderType(MessageFolderType.INBOX)
                        .isRead(false)
                        .isArchived(false)
                        .build())
                .toList();

        messageRecipientRepository.saveAll(recipientRows);

        // 발신자 SENT row 생성 (보낸 편지함)
        MessageRecipient senderRow = MessageRecipient.builder()
                .companyId(companyId)
                .message(saved)
                .employee(sender)
                .messageFolderType(MessageFolderType.SENT)
                .isRead(true) // 본인이 보낸 거라 읽음 처리해도 무방
                .isArchived(false)
                .build();

        messageRecipientRepository.save(senderRow);

        // 수신자들에게 알림 전송 (INBOX 레코드 기준)
        recipientRows.forEach(row -> {
            String notificationMessage = String.format("%s님이 메시지를 보냈습니다.\n제목: %s",
                    sender.getName(),
                    saved.getMessageTitle());

            notificationCommandService.createNotification(
                    companyId,
                    row.getEmployee().getId(),
                    NotificationType.MESSAGE,
                    notificationMessage,
                    "/view/message/detail?messageId=" + saved.getId() + "&folder=INBOX");
        });

        return saved.getId();
    }

    /** 메시지 삭제(소프트 삭제: 내 recipient row만 deletedAt 처리) */
    @Transactional
    public void deleteMessage(Long companyId, Long employeeId, Long messageId) {
        MessageRecipient mr = messageRecipientRepository
                .findByCompanyIdAndMessage_IdAndEmployee_IdAndDeletedAtIsNull(companyId, messageId, employeeId)
                .orElseThrow(() -> new NotFoundException("삭제할 메시지가 존재하지 않습니다."));

        // 메시지에 첨부된 파일이 있고, 해당 메시지의 모든 recipient가 삭제된 경우에만 파일 삭제
        Message message = mr.getMessage();
        if (message.getFiles() != null && !message.getFiles().isEmpty()) {
            // 해당 메시지의 모든 recipient가 삭제되었는지 확인
            // 현재 삭제하려는 recipient를 제외하고 카운트
            long activeRecipientCount = messageRecipientRepository
                    .countByMessage_IdAndDeletedAtIsNull(messageId);

            if (activeRecipientCount <= 1) {
                // 마지막 recipient이므로 파일들도 삭제
                for (File file : message.getFiles()) {
                    if (file.getObjectKey() != null) {
                        deleteObject(file.getObjectKey());
                    }
                }
            }
        }

        mr.softDelete();
    }

    /** 보관함 이동 */
    @Transactional
    public void archiveMessage(Long companyId, Long employeeId, Long messageId) {
        MessageRecipient mr = messageRecipientRepository
                .findByCompanyIdAndMessage_IdAndEmployee_IdAndDeletedAtIsNull(companyId, messageId, employeeId)
                .orElseThrow(() -> new NotFoundException("보관할 메시지가 존재하지 않습니다."));

        mr.archive();
    }

    /** 보관함 해제(원래 폴더로 복귀 = folderType 유지 + isArchived만 false) */
    @Transactional
    public void unarchiveMessage(Long companyId, Long employeeId, Long messageId) {
        MessageRecipient mr = messageRecipientRepository
                .findByCompanyIdAndMessage_IdAndEmployee_IdAndDeletedAtIsNull(companyId, messageId, employeeId)
                .orElseThrow(() -> new NotFoundException("보관 해제할 메시지가 존재하지 않습니다."));

        mr.unarchive();
    }

    /** 안 읽은 메시지 카운트 (받은 메시지함 기준) */
    public long getUnreadMessageCount(Long companyId, Long employeeId) {
        return messageRecipientRepository
                .countByCompanyIdAndEmployee_IdAndDeletedAtIsNullAndIsArchivedFalseAndMessageFolderTypeAndIsReadFalse(
                        companyId, employeeId, MessageFolderType.INBOX);
    }

    /** 파일 크기 검증 (50MB 제한) */
    private void validateFileSize(java.util.List<MultipartFile> files) {
        if (files != null) {
            long maxSize = 50 * 1024 * 1024; // 50MB
            for (MultipartFile file : files) {
                if (file.getSize() > maxSize) {
                    throw new InvalidRequestException("파일 크기는 50MB를 초과할 수 없습니다: " + file.getOriginalFilename());
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
