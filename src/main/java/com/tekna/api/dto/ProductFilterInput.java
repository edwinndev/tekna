package com.tekna.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProductFilterInput {

    private String name;
    private String sku;
    private UUID categoryId;
    private Boolean lowStockOnly;
}
