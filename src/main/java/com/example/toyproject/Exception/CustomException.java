package com.example.toyproject.Exception;

import lombok.Getter;

@Getter
//RuntimeException: 자바에 내장된 클래스로, ArithmeticException 등이 있음
public class CustomException extends RuntimeException {
    private final String code;
    //error를 발생시킬 때, 외부에서 전달한 error 코드와 메시지를 클래스 내부에 세팅
    public CustomException(String code, String message) {
        super(message);
        this.code = code;
    }
}
