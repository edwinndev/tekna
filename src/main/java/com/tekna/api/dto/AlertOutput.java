package com.tekna.api.dto;

import com.tekna.api.entity.AlertStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class AlertOutput {

    private UUID id;
    private UUID productId;
    private String productSku;
    private String productName;
    private Integer stockRecorded;
    private Integer minStockSnapshot;
    private AlertStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
