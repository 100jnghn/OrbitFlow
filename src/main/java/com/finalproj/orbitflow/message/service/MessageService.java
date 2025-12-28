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
import com.finalproj.orbitflow.message.repository.MessageRecipientRepository;
import com.finalproj.orbitflow.message.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            Pageable pageable
    ) {
        Page<MessageRecipient> page = archived
                ? messageRecipientRepository.findByCompanyIdAndEmployee_IdAndDeletedAtIsNullAndIsArchivedTrueOrderByCreatedAtDesc(
                companyId, employeeId, pageable
        )
                : messageRecipientRepository.findByCompanyIdAndEmployee_IdAndDeletedAtIsNullAndIsArchivedFalseAndMessageFolderTypeOrderByCreatedAtDesc(
                companyId, employeeId, folder, pageable
        );

        // peerName 정책:
        // - INBOX: senderName
        // - SENT: "수신자 N명" 또는 대표 1명
        return page.map(mr -> {
            String peerName;
            if (mr.getMessageFolderType() == MessageFolderType.INBOX) {
                peerName = mr.getMessage().getSender().getName();
            } else {
                peerName = "수신자"; // 필요하면 나중에 “N명” 같은 식으로 확장
            }
            return MessageResDto.ListItem.from(mr, peerName);
        });
    }

    /** 메시지 상세 조회 (+ 받은 편지함이면 읽음 처리) */
    @Transactional
    public MessageResDto.Detail getMessageDetail(
            Long companyId,
            Long employeeId,
            Long messageId
    ) {
        MessageRecipient mr = messageRecipientRepository
                .findByCompanyIdAndMessage_IdAndEmployee_IdAndDeletedAtIsNull(companyId, messageId, employeeId)
                .orElseThrow(() -> new NotFoundException("메시지가 존재하지 않습니다."));

        // 받은 편지함이면 읽음 처리
        if (mr.getMessageFolderType() == MessageFolderType.INBOX) {
            mr.markRead();
        }

        return MessageResDto.Detail.from(mr);
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
