package com.tekna.api.dto;

import com.tekna.api.entity.MovementType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class InventoryMovementOutput {

    private UUID id;
    private UUID productId;
    private String productName;
    private String productSku;
    private UUID supplierId;
    private String supplierName;
    private MovementType movementType;
    private Integer quantity;
    private Integer previousStock;
    private Integer newStock;
    private BigDecimal unitPrice;
    private String documentNumber;
    private String reason;
    private Instant createdAt;
}
