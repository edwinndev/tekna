package com.tekna.api.movement.service;

import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;
import com.tekna.api.movement.dto.CreateInventoryMovementInput;
import com.tekna.api.movement.dto.InventoryMovementFilterInput;
import com.tekna.api.movement.dto.InventoryMovementOutput;

import java.util.UUID;

public interface InventoryMovementService {

    InventoryMovementOutput registerMovement(CreateInventoryMovementInput input);

    InventoryMovementOutput getById(UUID id);

    PageOutput<InventoryMovementOutput> search(SearchInput<InventoryMovementFilterInput> input);
}
