package com.finalproj.orbitflow.global.exception;

import com.finalproj.orbitflow.global.common.ResponseDto;
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
}