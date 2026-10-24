package com.tekna.api.dto;

import com.tekna.api.entity.AlertStatus;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class AlertFilterInput {
    private UUID productId;
    private AlertStatus status;
    private String productQuery;
}
