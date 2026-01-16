package com.finalproj.orbitflow.global.exception;

import com.finalproj.orbitflow.global.common.ResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

/**
 * 전역 예외 처리기.
 * 애플리케이션 전반에서 발생하는 예외를 공통 응답(ResponseDto) 형식으로 변환한다.
 *
 * @author : seunga03
 * @filename : GlobalExceptionHandler
 * @since : 2025-12-18 목요일
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    /**
     * throw new UnauthorizedException("로그인이 필요합니다.");
     **/
    @ExceptionHandler(UnauthorizedException.class) // → 401
    public ResponseEntity<?> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(HttpStatus.UNAUTHORIZED, e.getMessage(), null));
    }


    /**
     * throw new ForbiddenException("접근 권한이 없습니다.");
     **/
    @ExceptionHandler(ForbiddenException.class) // → 403
    public ResponseEntity<?> handleForbidden(ForbiddenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseDto<>(HttpStatus.FORBIDDEN, e.getMessage(), null));
    }


    /**
     * throw new NotFoundException("존재하지 않는 리소스입니다.");
     **/
    @ExceptionHandler(NotFoundException.class) // → 404
    public ResponseEntity<?> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDto<>(HttpStatus.NOT_FOUND, e.getMessage(), null));
    }


    /**
     * throw new BusinessException("이미 처리된 상태입니다.");
     **/
    @ExceptionHandler(BusinessException.class) // → 409
    public ResponseEntity<?> handleBusiness(BusinessException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseDto<>(HttpStatus.CONFLICT, e.getMessage(), null));
    }

    /**
     * throw new ConfirmRequiredException("확인이 필요합니다");
     **/
    @ExceptionHandler(ConfirmRequiredException.class) // → 409
    public ResponseEntity<?> handleConfirm(ConfirmRequiredException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseDto<>(HttpStatus.CONFLICT, e.getMessage(), null));
    }

    /**
     * throw new InvalidRequestException("잘못된 요청입니다.");
     **/
    @ExceptionHandler(InvalidRequestException.class) // → 400
    public ResponseEntity<?> handleInvalidRequest(InvalidRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDto<>(HttpStatus.BAD_REQUEST, e.getMessage(), null));
    }

    /**
     * throw new InvalidStateException("현재 상태에서는 수행할 수 없습니다.");
     **/
    @ExceptionHandler(InvalidStateException.class) // → 409
    public ResponseEntity<?> handleInvalidState(InvalidStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseDto<>(HttpStatus.CONFLICT, e.getMessage(), null));
    }

    @ExceptionHandler(DuplicateCarNumberException.class)
    public ResponseEntity<?> handleDuplicateCarNumber(DuplicateCarNumberException e) {
        log.info(e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseDto<>(HttpStatus.CONFLICT, e.getMessage(), null));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.badRequest().body(new ResponseDto<>(HttpStatus.BAD_REQUEST, e.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream().map(err -> err.getField() + " : " + err.getDefaultMessage()).findFirst().orElse("Invalid request");

        log.error("❌ Validation failed", e);

        return ResponseEntity.badRequest().body(new ResponseDto<>(HttpStatus.BAD_REQUEST, message, null));
    }

    @ExceptionHandler(RealTimeAccessException.class)
    public ResponseEntity<?> handleRealTimeAccess(RealTimeAccessException e) {
        log.warn("SSE 연결이 아닌 상태");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception e) {
        log.error("🔥 UNHANDLED EXCEPTION", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), null));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> handleIOException(IOException e, HttpServletRequest request) {

        // SSE 요청인지 판별 (Accept 또는 produces 기준)
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        boolean isSse = accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE);

        // SSE 연결 요청
        if (isSse) {
            // SSE에서는 JSON 응답을 만들면 안 됨
            // 클라이언트가 탭 닫거나 네트워크 끊겨서 나는 정상 케이스.
            log.debug("SSE IOException (likely client disconnect): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        // 일반 API 요청은 기존처럼 JSON 응답
        log.warn("IOException 발생: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), null));
    }
}