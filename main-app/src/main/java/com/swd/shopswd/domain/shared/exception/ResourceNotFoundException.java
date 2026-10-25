package com.swd.shopswd.domain.shared.exception;

/**
 * 식별자로 리소스를 찾지 못한 경우.
 */
public abstract class ResourceNotFoundException extends DomainException {

    protected ResourceNotFoundException(String code, String message) {
        super(code, message);
    }
}
