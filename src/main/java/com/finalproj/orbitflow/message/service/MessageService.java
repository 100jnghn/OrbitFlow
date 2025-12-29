package com.finalproj.orbitflow.message.service;

import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageRecipientRepository messageRecipientRepository;
    private final EmployeeRepository employeeRepository;

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
            Pageable pageable
    ) {
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
                        companyId, employeeId, startInstant, endExclusiveInstant, searchType, keyword
                );
                // 정렬을 pageable에 포함
                pageable = org.springframework.data.domain.PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                );
                page = messageRecipientRepository.findAll(spec, pageable);
            } else {
                page = messageRecipientRepository.findByCompanyIdAndEmployee_IdAndDeletedAtIsNullAndIsArchivedTrueOrderByCreatedAtDesc(
                        companyId, employeeId, pageable
                );
            }
        } else {
            if (hasSearch || hasDateFilter) {
                Specification<MessageRecipient> spec = MessageRecipientSpecifications.listSpec(
                        companyId, employeeId, folder, startInstant, endExclusiveInstant, searchType, keyword
                );
                // 정렬을 pageable에 포함
                pageable = org.springframework.data.domain.PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                );
                page = messageRecipientRepository.findAll(spec, pageable);
            } else {
                page = messageRecipientRepository.findByCompanyIdAndEmployee_IdAndDeletedAtIsNullAndIsArchivedFalseAndMessageFolderTypeOrderByCreatedAtDesc(
                        companyId, employeeId, folder, pageable
                );
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
                            .recipientId(recipient.getId())  // INBOX 레코드의 ID 사용
                            .folderType(MessageFolderType.SENT)  // 표시는 SENT로
                            .archived(recipient.isArchived())
                            .read(recipient.isRead())  // 수신자의 읽음 상태
                            .readAt(recipient.getReadAt())  // 수신자가 읽은 일시
                            .title(recipient.getMessage().getMessageTitle())
                            .peerName(peerName)  // 수신자 이름
                            .senderName(recipient.getMessage().getSender().getName())
                            .recipientName(null)
                            .createdAt(recipient.getMessage().getCreatedAt())  // 메시지 생성일 (발신일)
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
                    resultList.size()
            );
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
            Long recipientId  // 보낸 메시지함에서 특정 수신자 선택 시 사용 (optional)
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

        // 받은 편지함이면 읽음 처리
        if (mr.getMessageFolderType() == MessageFolderType.INBOX) {
            mr.markRead();
        }

        MessageResDto.Detail detail = MessageResDto.Detail.from(mr);
        
        // 보낸 메시지함인 경우 수신자 정보 추가
        // recipientId가 제공된 경우 (보낸 메시지함에서 특정 수신자 선택)
        if (recipientId != null || (mr.getMessageFolderType() == MessageFolderType.INBOX && 
            mr.getMessage().getSender().getId().equals(employeeId))) {
            // 보낸 메시지함: 현재 레코드가 INBOX이지만 발신자가 현재 사용자
            // 즉, 보낸 메시지함에서 특정 수신자를 선택한 경우
            return MessageResDto.Detail.builder()
                    .messageId(detail.getMessageId())
                    .recipientId(detail.getRecipientId())
                    .title(detail.getTitle())
                    .content(detail.getContent())
                    .senderId(detail.getSenderId())
                    .senderName(detail.getSenderName())
                    .recipientIdDetail(mr.getEmployee().getId())
                    .recipientName(mr.getEmployee().getName())
                    .folderType(MessageFolderType.SENT)  // 보낸 메시지함으로 표시
                    .archived(detail.isArchived())
                    .read(detail.isRead())
                    .readAt(detail.getReadAt())
                    .fileId(detail.getFileId())
                    .createdAt(detail.getCreatedAt())
                    .build();
        }
        
        return detail;
    }

    /** 메시지 전송 */
    @Transactional
    public Long sendMessage(Long companyId, Long senderEmployeeId, MessageReqDto.Send request) {

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

        // Message(본문) 생성
        Message message = Message.builder()
                .companyId(companyId)
                .sender(sender)
                .messageTitle(request.getMessageTitle())
                .messageContent(request.getMessageContent())
                .fileId(request.getFileId()) // 자리만
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
                .isRead(true)      // 본인이 보낸 거라 읽음 처리해도 무방
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


}
