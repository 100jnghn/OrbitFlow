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
        private Instant readAt;       // 읽은 일시

        private String title;
        private String peerName;      // INBOX: 보낸 사람, SENT: 받는 사람(대표 1명만 표시 등 정책 필요)
        private String senderName;    // 발신자 이름 (보관함용)
        private String recipientName; // 수신자 이름 (보관함용)
        private Instant createdAt;

        public static ListItem from(MessageRecipient mr, String peerName) {
            return ListItem.builder()
                    .messageId(mr.getMessage().getId())
                    .recipientId(mr.getId())
                    .folderType(mr.getMessageFolderType())
                    .archived(mr.isArchived())
                    .read(mr.isRead())
                    .readAt(mr.getReadAt())
                    .title(mr.getMessage().getMessageTitle())
                    .peerName(peerName)
                    .senderName(mr.getMessage().getSender().getName())
                    .recipientName(null)  // 보관함에서만 사용, 나중에 필요하면 설정
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
        
        private Long recipientIdDetail;  // 수신자 ID (보낸 메시지 상세에서 사용)
        private String recipientName;    // 수신자 이름 (보낸 메시지 상세에서 사용)

        private MessageFolderType folderType; // 원래 폴더
        private boolean archived;

        private boolean read;
        private Instant readAt;

        private Long fileId;          // 자리만
        private Instant createdAt;

        public static Detail from(MessageRecipient mr) {
            // SENT 폴더인 경우 수신자 정보 추가
            String recipientName = null;
            Long recipientIdDetail = null;
            if (mr.getMessageFolderType() == MessageFolderType.SENT) {
                // SENT 레코드에서 실제 수신자 정보는 INBOX 레코드를 통해 얻어야 함
                // 일단 null로 설정하고, 필요시 Service에서 설정
            }
            
            return Detail.builder()
                    .messageId(mr.getMessage().getId())
                    .recipientId(mr.getId())
                    .title(mr.getMessage().getMessageTitle())
                    .content(mr.getMessage().getMessageContent())
                    .senderId(mr.getMessage().getSender().getId())
                    .senderName(mr.getMessage().getSender().getName())
                    .recipientIdDetail(recipientIdDetail)
                    .recipientName(recipientName)
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
