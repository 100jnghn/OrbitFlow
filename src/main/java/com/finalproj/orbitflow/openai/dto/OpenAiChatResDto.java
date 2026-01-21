package com.finalproj.orbitflow.openai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : OpenAiChatResDto
 * @since : 2025-12-30 오후 11:50 화요일
 */

@Getter
@Setter
@NoArgsConstructor
public class OpenAiChatResDto {

    private List<Choice> choices;

    @Getter
    @NoArgsConstructor
    public static class Choice {
        private Message message;
    }

    @Getter
    @NoArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}
