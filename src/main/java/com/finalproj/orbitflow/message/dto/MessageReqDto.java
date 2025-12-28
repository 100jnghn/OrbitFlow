package com.finalproj.orbitflow.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

public class MessageReqDto {

    /** 메시지 전송 */
    @Getter
    public static class Send {
        @NotBlank
        private String messageTitle;

        @NotBlank
        private String messageContent;

        /** 수신자 employeeId 리스트 */
        @NotEmpty
        private List<Long> recipientEmployeeIds;

        /** 파일은 우선 자리만 */
        private Long fileId;
    }
}
