package com.tekna.api.dto;

import com.tekna.api.entity.MovementType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class InventoryMovementFilterInput {

    private UUID productId;
    private UUID supplierId;
    private MovementType movementType;
    private Instant startDate;
    private Instant endDate;
}
