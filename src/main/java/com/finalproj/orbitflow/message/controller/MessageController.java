package com.finalproj.orbitflow.message.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.message.dto.MessageResDto;
import com.finalproj.orbitflow.message.enums.MessageFolderType;
import com.finalproj.orbitflow.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    /** 메시지함 목록 */
    @GetMapping
    public ResponseEntity<ResponseDto<Page<MessageResDto.ListItem>>> getMessageList(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false, defaultValue = "INBOX") MessageFolderType folder,
            @RequestParam(required = false, defaultValue = "false") boolean archived,
            Pageable pageable
    ) {
        Page<MessageResDto.ListItem> result = messageService.getMessageList(
                user.getCompanyId(),
                user.getEmployeeId(),
                folder,
                archived,
                pageable
        );

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "메시지 목록 조회 성공", result));
    }

    /** 메시지 상세 조회 */
    @GetMapping("/{messageId}")
    public ResponseEntity<ResponseDto<MessageResDto.Detail>> getMessageDetail(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long messageId
    ) {
        MessageResDto.Detail result = messageService.getMessageDetail(
                user.getCompanyId(),
                user.getEmployeeId(),
                messageId
        );

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "메시지 상세 조회 성공", result));
    }

}
