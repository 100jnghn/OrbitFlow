package com.finalproj.orbitflow.message.service;

import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.message.dto.MessageResDto;
import com.finalproj.orbitflow.message.entity.MessageRecipient;
import com.finalproj.orbitflow.message.enums.MessageFolderType;
import com.finalproj.orbitflow.message.repository.MessageRecipientRepository;
import com.finalproj.orbitflow.message.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


}
