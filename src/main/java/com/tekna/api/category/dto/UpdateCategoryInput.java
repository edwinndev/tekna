package com.tekna.api.category.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateCategoryInput extends CreateCategoryInput {

    @NotNull(message = "ID is required for update")
    private UUID id;
}
