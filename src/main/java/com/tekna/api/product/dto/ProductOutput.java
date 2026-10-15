package com.tekna.api.product.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ProductOutput {

    private UUID id;
    private UUID categoryId;
    private String categoryName;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Integer minStock;
    private Boolean isLowStock;
    private Instant createdAt;
    private Instant updatedAt;
}
