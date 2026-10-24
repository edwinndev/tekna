package com.tekna.api.common.exception;

public class InsufficientStockException extends BadRequestException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
