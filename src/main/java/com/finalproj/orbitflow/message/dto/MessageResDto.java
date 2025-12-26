package com.finalproj.orbitflow.message.dto;

import com.finalproj.orbitflow.message.entity.Message;
import com.finalproj.orbitflow.message.entity.MessageRecipient;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

public class MessageResDto {

    @Getter
    @Builder
    public static class ListItem {
        private Long messageId;
        private String title;
        private String senderName;
        private boolean isRead;
        private Instant createdAt;

        // 받은함/보관함: MessageRecipient 기준
        public static ListItem from(MessageRecipient mr) {
            return ListItem.builder()
                    .messageId(mr.getMessage().getId())
                    .title(mr.getMessage().getTitle())
                    .senderName(mr.getMessage().getSender().getName())
                    .isRead(mr.isRead())
                    .createdAt(mr.getCreatedAt())
                    .build();
        }

        // 보낸함(SENT): Message 기준
        public static ListItem fromSent(Message m) {
            return ListItem.builder()
                    .messageId(m.getId())
                    .title(m.getTitle())
                    .senderName(m.getSender().getName())
                    .isRead(false)                 // 보낸함 read 정책 없으면 일단 false
                    .createdAt(m.getCreatedAt())
                    .build();
        }

    }


    @Getter
    @Builder
    public static class Detail {
        private Long messageId;
        private String title;
        private String content;
        private String senderName;
        private Instant createdAt;

        public static Detail from(Message message) {
            return Detail.builder()
                    .messageId(message.getId())
                    .title(message.getTitle())
                    .content(message.getContent())
                    .senderName(message.getSender().getName())
                    .createdAt(message.getCreatedAt())
                    .build();
        }
    }

}
