package com.finalproj.orbitflow.message.service;

import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.global.file.enums.FileDomain;
import com.finalproj.orbitflow.global.file.service.FileService;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import com.finalproj.orbitflow.message.dto.MessageReqDto;
import com.finalproj.orbitflow.message.dto.MessageResDto;
import com.finalproj.orbitflow.message.entity.Message;
import com.finalproj.orbitflow.message.entity.MessageRecipient;
import com.finalproj.orbitflow.message.enums.MessageFolderType;
import com.finalproj.orbitflow.message.enums.MessageSearchType;
import com.finalproj.orbitflow.message.repository.MessageRecipientRepository;
import com.finalproj.orbitflow.message.repository.MessageRecipientSpecifications;
import com.finalproj.orbitflow.message.repository.MessageRepository;
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
            if (hasSearch || hasDateFilter) {
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

        // SENT 폴더인 경우: 각 수신자마다 별도 행으로 표시하기 위해 INBOX 레코드 사용
        if (!archived && folder == MessageFolderType.SENT) {
            // SENT 폴더 조회 시: 해당 메시지의 모든 INBOX 수신자 레코드를 조회
            // 각 수신자마다 별도 행으로 표시하기 위함
            List<MessageResDto.ListItem> resultList = new java.util.ArrayList<>();

            // 먼저 SENT 레코드로 메시지 ID 목록 조회
            List<Long> messageIds = page.getContent().stream()
                    .map(mr -> mr.getMessage().getId())
                    .distinct()
                    .toList();

            // 각 메시지의 INBOX 수신자 레코드 조회
            for (Long messageId : messageIds) {
                List<MessageRecipient> recipients = messageRecipientRepository
                        .findByMessage_IdAndMessageFolderTypeAndDeletedAtIsNull(
                                messageId, MessageFolderType.INBOX);

                for (MessageRecipient recipient : recipients) {
                    // 각 수신자마다 별도 ListItem 생성
                    String peerName = recipient.getEmployee().getName();
                    MessageResDto.ListItem item = MessageResDto.ListItem.builder()
                            .messageId(recipient.getMessage().getId())
                            .recipientId(recipient.getId()) // INBOX 레코드의 ID 사용
                            .folderType(MessageFolderType.SENT) // 표시는 SENT로
                            .archived(recipient.isArchived())
                            .read(recipient.isRead()) // 수신자의 읽음 상태
                            .readAt(recipient.getReadAt()) // 수신자가 읽은 일시
                            .title(recipient.getMessage().getMessageTitle())
                            .peerName(peerName) // 수신자 이름
                            .senderName(recipient.getMessage().getSender().getName())
                            .recipientName(null)
                            .createdAt(recipient.getMessage().getCreatedAt()) // 메시지 생성일 (발신일)
                            .build();
                    resultList.add(item);
                }
            }

            // 정렬: 생성일 기준 내림차순
            resultList.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

            // 페이징 처리
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), resultList.size());
            List<MessageResDto.ListItem> pagedList = resultList.subList(start, end);

            // Page 객체로 변환
            return new org.springframework.data.domain.PageImpl<>(
                    pagedList,
                    pageable,
                    resultList.size());
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
        MessageRecipient mr;

        // recipientId가 제공되면 해당 레코드 조회 (보낸 메시지함에서 INBOX recipientId 사용)
        if (recipientId != null) {
            mr = messageRecipientRepository.findById(recipientId)
                    .orElseThrow(() -> new NotFoundException("메시지가 존재하지 않습니다."));
            // 권한 확인: 메시지가 해당 회사의 것이고, 해당 수신자 레코드인지 확인
            if (!mr.getCompanyId().equals(companyId) || !mr.getMessage().getId().equals(messageId)) {
                throw new ForbiddenException("메시지에 접근할 수 없습니다.");
            }
            // 보낸 메시지함인 경우: 메시지의 발신자가 현재 사용자인지 확인
            if (!mr.getMessage().getSender().getId().equals(employeeId)) {
                throw new ForbiddenException("메시지에 접근할 수 없습니다.");
            }
        } else {
            // 기존 방식: messageId와 employeeId로 조회
            mr = messageRecipientRepository
                    .findByCompanyIdAndMessage_IdAndEmployee_IdAndDeletedAtIsNull(companyId, messageId, employeeId)
                    .orElseThrow(() -> new NotFoundException("메시지가 존재하지 않습니다."));
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
            List<MultipartFile> files) {

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

        // 파일 업로드 처리
        List<File> attachedFiles = null;
        if (files != null && !files.isEmpty()) {
            attachedFiles = files.stream()
                    .map(file -> fileService.upload(companyId, senderEmployeeId, FileDomain.MESSAGE, file))
                    .toList();
        }

        // Message(본문) 생성
        Message message = Message.builder()
                .companyId(companyId)
                .sender(sender)
                .messageTitle(request.getMessageTitle())
                .messageContent(request.getMessageContent())
                .files(attachedFiles)
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
                // 마지막 recipient이므로 파일도 삭제
                List<File> files = message.getFiles();
                files.forEach(file -> {
                    if (file.getObjectKey() != null) {
                        fileService.deleteObject(file.getObjectKey());
                    }
                });

                // 파일 엔티티는 CascadeType.ALL에 의해 메시지가 삭제될 때 같이 삭제되거나
                // 직접 삭제할 수도 있음. 여기서는 S3 삭제만 수행하고 DB 삭제는 메시지 삭제 시점 혹은 고아 객체 제거로 위임 가능
                // 하지만 MessageRecipient soft delete 방식이라 Message 자체는 살아있음.
                // 따라서 파일만 명시적으로 삭제하거나, 리스트를 비워서 orphanRemoval 유도
                // message.getFiles().clear(); // 이것은 더티 체킹으로 반영됨

                // 로직상 Message가 여러 사람에게 공유되므로, 모든 사람이 삭제했을 때만 파일 삭제
                // 하지만 soft delete라서 Message 자체는 남아있음.
                // 완전히 삭제하려면 Message도 soft delete 처리해야 하는데, 현재 로직에는 없음.
                // (Message 엔티티에는 deletedAt 없음, MessageRecipient에만 있음) -> 아님, Message에도
                // deletedAt 있음?
                // Message 엔티티 BaseEntity 상속받았으나 softDelete 메소드 없음... 아까 BaseEntity 확인함.
                // 확인해보니 Message 엔티티 자체는 softDelete 호출 안함.
                // MessageRecipient만 soft delete 함. Message는 영원히 남는 구조인가?
                // 네, MessageService.deleteMessage 로직을 보면 recipient만 soft delete함.

                // 그렇다면 파일도 실제로 삭제하면 안될 수도 있음 (나중에 복구하거나 감사를 위해)
                // 하지만 기존 코드에서는 S3에서 삭제하고 있었음.
                // 기존 로직 유지: 마지막 사람이 삭제하면 S3 파일 삭제.
                // 그리고 DB상의 File 레코드는 orphanRemoval=true에 의해
                // message.getFiles().clear() 하면 삭제됨.

                // message.getFiles().clear(); // TODO: 이거 호출하면 DB에서도 삭제됨 (orphanRemoval)
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
}
