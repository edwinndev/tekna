package com.tekna.api.movement.dto;

import com.tekna.api.movement.persistence.entity.MovementType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CreateInventoryMovementInput {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Movement type is required (ENTRY or EXIT)")
    private MovementType movementType;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @DecimalMin(value = "0.0", message = "Unit price must be zero or positive")
    private BigDecimal unitPrice;

    @Size(max = 100, message = "Document number cannot exceed 100 characters")
    private String documentNumber;

    private UUID supplierId;

    @Size(max = 255, message = "Reason cannot exceed 255 characters")
    private String reason;
}
