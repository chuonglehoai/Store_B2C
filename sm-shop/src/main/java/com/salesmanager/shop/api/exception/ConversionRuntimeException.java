package com.salesmanager.shop.api.exception;

public class ConversionRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConversionRuntimeException() {
        super();
    }

    public ConversionRuntimeException(String message) {
        super(message);
    }

    public ConversionRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConversionRuntimeException(Throwable cause) {
        super(cause);
    }
}