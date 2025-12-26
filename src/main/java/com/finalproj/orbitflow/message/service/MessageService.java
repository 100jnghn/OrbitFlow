package com.finalproj.orbitflow.message.service;

import com.finalproj.orbitflow.global.exception.NotFoundException;
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

    /** 메시지 목록 조회 (받은/보낸/보관함) */
    public Page<MessageResDto.ListItem> getMessageList(
            Long companyId,
            Long employeeId,
            MessageFolderType folderType,
            Pageable pageable
    ) {
        if (folderType == MessageFolderType.SENT) {
            return messageRepository
                    .findByCompanyIdAndSender_IdOrderByCreatedAtDesc(
                            companyId,
                            employeeId,
                            pageable
                    )
                    .map(MessageResDto.ListItem::fromSent);
        }
        return messageRecipientRepository
                .findByCompanyIdAndRecipient_IdAndFolderTypeAndDeletedAtIsNull(
                        companyId,
                        employeeId,
                        folderType,
                        pageable
                )
                .map(MessageResDto.ListItem::from);
    }

    /** 메시지 상세 조회 */
    @Transactional
    public MessageResDto.Detail getMessageDetail(
            Long companyId,
            Long employeeId,
            Long messageId
    ) {
        MessageRecipient mr = messageRecipientRepository
                .findByCompanyIdAndMessage_IdAndRecipient_IdAndDeletedAtIsNull(
                        companyId,
                        messageId,
                        employeeId
                )
                .orElseThrow(() -> new NotFoundException("메시지를 찾을 수 없습니다."));

        mr.markAsRead();

        return MessageResDto.Detail.from(mr.getMessage());
    }
}
