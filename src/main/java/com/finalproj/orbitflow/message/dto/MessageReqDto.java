package com.finalproj.orbitflow.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class MessageReqDto {

    /** 메시지 전송 */
    @Getter
    @Setter
    public static class Send {
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        private String messageTitle;

        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 3000, message = "내용은 3,000자 이하여야 합니다.")
        private String messageContent;

        /** 수신자 employeeId 리스트 */
        @NotEmpty(message = "수신자를 최소 1명 이상 선택해주세요.")
        private List<Long> recipientEmployeeIds;

        // 파일은 별도 파라미터로 받음
    }
}
