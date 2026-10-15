package com.tekna.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class SupplierOutput {

    private UUID id;
    private String name;
    private String contactName;
    private String email;
    private String phone;
    private String address;
    private Instant createdAt;
    private Instant updatedAt;
}
