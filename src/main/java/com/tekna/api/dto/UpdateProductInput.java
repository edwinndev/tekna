package com.tekna.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateProductInput extends CreateProductInput {

    @NotNull(message = "ID is required for update")
    private UUID id;
}
