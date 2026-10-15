package com.tekna.api.supplier.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateSupplierInput extends CreateSupplierInput {

    @NotNull(message = "ID is required for update")
    private UUID id;
}
