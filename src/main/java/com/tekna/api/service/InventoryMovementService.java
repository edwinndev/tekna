package com.tekna.api.service;

import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;
import com.tekna.api.dto.CreateInventoryMovementInput;
import com.tekna.api.dto.InventoryMovementFilterInput;
import com.tekna.api.dto.InventoryMovementOutput;

import java.util.UUID;

public interface InventoryMovementService {

    InventoryMovementOutput registerMovement(CreateInventoryMovementInput input);

    InventoryMovementOutput getById(UUID id);

    PageOutput<InventoryMovementOutput> search(SearchInput<InventoryMovementFilterInput> input);
}
