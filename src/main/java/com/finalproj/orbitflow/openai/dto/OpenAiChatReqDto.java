package com.finalproj.orbitflow.openai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : OpenAiChatReqDto
 * @since : 26. 1. 5. 월요일
 **/


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenAiChatReqDto {

    private String model;
    private List<Message> messages;
    private Double temperature;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}