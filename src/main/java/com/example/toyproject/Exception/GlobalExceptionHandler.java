package com.example.toyproject.Exception;

import com.example.toyproject.DTO.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//앱 전체에서 발생하는 예외를 한 곳에서 잡아서 처리
//프로젝트 전체의 Controller에서 발생한 예외를 감시하고 처리하는 객체
//ControllerAdvice + ResponseBody
@RestControllerAdvice
public class GlobalExceptionHandler {

    //CustomException 발생 시, spring이 자동으로 이 메서드를 실행하여 에러 객체(e)를 넘겨줌
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException e) {
        //그에 대응한 HTTP 상태 코드 결정
        int status = switch (e.getCode()) {
            case "NOT_FOUND" -> 404;
            case "MISSING_IMAGE", "INVALID_FIELD" -> 400;
            case "FORBIDDEN" -> 403;
            default -> 500;
        };
        //HTTP 상태 코드와 공통 에러 규격(ApiResponse.fail)을 결합하여 최종 응답 반환
        return ResponseEntity.status(status)
                .body(ApiResponse.fail(e.getCode(), e.getMessage()));
    }

    //@Valid 검증 실패 시 (NotBlank, Size 등)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                //각 에러에서 구체적인 경고 메세지 추출
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("입력값을 확인해주세요");
        //사용자의 입력 잘못으로 발생한 에러 -> 400
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail("INVALID_FIELD", message));
    }
}
