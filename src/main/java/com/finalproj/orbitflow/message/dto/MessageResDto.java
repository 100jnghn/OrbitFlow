package com.finalproj.orbitflow.message.dto;

import com.finalproj.orbitflow.message.entity.MessageRecipient;
import com.finalproj.orbitflow.message.enums.MessageFolderType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

public class MessageResDto {

    /** 목록 아이템 */
    @Getter
    @Builder
    public static class ListItem {
        private Long messageId;
        private Long recipientId;     // message_recipient PK (프론트에서 필요하면)
        private MessageFolderType folderType; // 원래 폴더(INBOX/SENT)
        private boolean archived;
        private boolean read;

        private String title;
        private String peerName;      // INBOX: 보낸 사람, SENT: 받는 사람(대표 1명만 표시 등 정책 필요)
        private Instant createdAt;

        public static ListItem from(MessageRecipient mr, String peerName) {
            return ListItem.builder()
                    .messageId(mr.getMessage().getId())
                    .recipientId(mr.getId())
                    .folderType(mr.getMessageFolderType())
                    .archived(mr.isArchived())
                    .read(mr.isRead())
                    .title(mr.getMessage().getMessageTitle())
                    .peerName(peerName)
                    .createdAt(mr.getCreatedAt())
                    .build();
        }
    }

    /** 상세 */
    @Getter
    @Builder
    public static class Detail {
        private Long messageId;
        private Long recipientId;

        private String title;
        private String content;

        private Long senderId;
        private String senderName;

        private MessageFolderType folderType; // 원래 폴더
        private boolean archived;

        private boolean read;
        private Instant readAt;

        private Long fileId;          // 자리만
        private Instant createdAt;

        public static Detail from(MessageRecipient mr) {
            return Detail.builder()
                    .messageId(mr.getMessage().getId())
                    .recipientId(mr.getId())
                    .title(mr.getMessage().getMessageTitle())
                    .content(mr.getMessage().getMessageContent())
                    .senderId(mr.getMessage().getSender().getId())
                    .senderName(mr.getMessage().getSender().getName())
                    .folderType(mr.getMessageFolderType())
                    .archived(mr.isArchived())
                    .read(mr.isRead())
                    .readAt(mr.getReadAt())
                    .fileId(mr.getMessage().getFileId())
                    .createdAt(mr.getMessage().getCreatedAt())
                    .build();
        }
    }
}
