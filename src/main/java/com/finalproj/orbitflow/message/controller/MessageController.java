package com.finalproj.orbitflow.message.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.message.dto.MessageReqDto;
import com.finalproj.orbitflow.message.dto.MessageResDto;
import com.finalproj.orbitflow.message.enums.MessageFolderType;
import com.finalproj.orbitflow.message.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        Page<MessageResDto.ListItem> result = messageService.getMessageList(
                user.getCompanyId(),
                user.getEmployeeId(),
                folder,
                archived,
                startDate,
                endDate,
                searchType,
                keyword,
                pageable
        );

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "메시지 목록 조회 성공", result));
    }

    /** 메시지 상세 조회 */
    @GetMapping("/{messageId}")
    public ResponseEntity<ResponseDto<MessageResDto.Detail>> getMessageDetail(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long messageId,
            @RequestParam(required = false) Long recipientId  // 보낸 메시지함에서 특정 수신자 선택 시
    ) {
        MessageResDto.Detail result = messageService.getMessageDetail(
                user.getCompanyId(),
                user.getEmployeeId(),
                messageId,
                recipientId
        );

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "메시지 상세 조회 성공", result));
    }

    /** 메시지 전송 */
    @PostMapping
    public ResponseEntity<ResponseDto<Long>> sendMessage(
            @AuthenticationPrincipal SecurityUser user,
            @RequestBody @Valid MessageReqDto.Send request
    ) {
        Long messageId = messageService.sendMessage(
                user.getCompanyId(),
                user.getEmployeeId(),
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDto<>(HttpStatus.CREATED, "메시지 전송 성공", messageId));
    }

    /** 메시지 삭제(소프트 삭제) */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<ResponseDto<Void>> deleteMessage(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long messageId
    ) {
        messageService.deleteMessage(user.getCompanyId(), user.getEmployeeId(), messageId);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "메시지 삭제 성공", null));
    }

    /** 보관함 이동 */
    @PatchMapping("/{messageId}/archive")
    public ResponseEntity<ResponseDto<Void>> archiveMessage(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long messageId
    ) {
        messageService.archiveMessage(user.getCompanyId(), user.getEmployeeId(), messageId);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "보관함 이동 성공", null));
    }

    /** 보관함 해제 */
    @PatchMapping("/{messageId}/unarchive")
    public ResponseEntity<ResponseDto<Void>> unarchiveMessage(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long messageId
    ) {
        messageService.unarchiveMessage(user.getCompanyId(), user.getEmployeeId(), messageId);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "보관함 해제 성공", null));
    }

}
