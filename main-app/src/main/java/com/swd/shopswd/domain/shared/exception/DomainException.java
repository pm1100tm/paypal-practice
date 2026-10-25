package com.swd.shopswd.domain.shared.exception;

/**
 * 도메인 규칙 위반 최상위 예외.
 * code 는 클라이언트가 분기 처리할 수 있는 안정적인 식별자이다.
 */
public abstract class DomainException extends RuntimeException {

    private final String code;

    protected DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    protected DomainException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
