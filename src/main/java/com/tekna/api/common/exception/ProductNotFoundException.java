package com.tekna.api.common.exception;

import java.util.UUID;

public class ProductNotFoundException extends NotFoundException {

    public ProductNotFoundException(UUID id) {
        super("Product not found with id: " + id);
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}
