package com.finalproj.orbitflow.global.exception;

import com.finalproj.orbitflow.global.common.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
    public ResponseEntity<ResponseDto> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDto(HttpStatus.UNAUTHORIZED, e.getMessage(), null));
    }


    /**
     * throw new ForbiddenException("접근 권한이 없습니다.");
     **/
    @ExceptionHandler(ForbiddenException.class) // → 403
    public ResponseEntity<ResponseDto> handleForbidden(ForbiddenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ResponseDto(HttpStatus.FORBIDDEN, e.getMessage(), null));
    }


    /**
     * throw new NotFoundException("존재하지 않는 리소스입니다.");
     **/
    @ExceptionHandler(NotFoundException.class) // → 404
    public ResponseEntity<ResponseDto> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDto(HttpStatus.NOT_FOUND, e.getMessage(), null));
    }


    /**
     * throw new BusinessException("이미 처리된 상태입니다.");
     **/
    @ExceptionHandler(BusinessException.class) // → 409
    public ResponseEntity<ResponseDto> handleBusiness(BusinessException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ResponseDto(HttpStatus.CONFLICT, e.getMessage(), null));
    }

    /**
     * throw new ConfirmRequiredException("확인이 필요합니다");
     **/
    @ExceptionHandler(ConfirmRequiredException.class) // → 409
    public ResponseEntity<ResponseDto> handleConfirm(ConfirmRequiredException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ResponseDto(HttpStatus.CONFLICT, e.getMessage(), null));
    }

    /**
     * throw new InvalidRequestException("잘못된 요청입니다.");
     **/
    @ExceptionHandler(InvalidRequestException.class) // → 400
    public ResponseEntity<ResponseDto> handleInvalidRequest(InvalidRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDto(HttpStatus.BAD_REQUEST, e.getMessage(), null));
    }

    /**
     * throw new InvalidStateException("현재 상태에서는 수행할 수 없습니다.");
     **/
    @ExceptionHandler(InvalidStateException.class) // → 409
    public ResponseEntity<ResponseDto> handleInvalidState(InvalidStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ResponseDto(HttpStatus.CONFLICT, e.getMessage(), null));
    }

    @ExceptionHandler(DuplicateCarNumberException.class)
    public ResponseEntity<ResponseDto> handleDuplicateCarNumber(DuplicateCarNumberException e) {
        log.info(e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ResponseDto(HttpStatus.CONFLICT, e.getMessage(), null));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ResponseDto> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.badRequest()
                .body(new ResponseDto(HttpStatus.BAD_REQUEST, e.getMessage(), null));
    }

}