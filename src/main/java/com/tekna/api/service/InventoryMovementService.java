package com.tekna.api.service;

import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;
import com.tekna.api.dto.CreateInventoryMovementInput;
import com.tekna.api.dto.InventoryMovementFilterInput;
import com.tekna.api.dto.InventoryMovementOutput;

import java.util.UUID;

/**
 * Service contract for managing inventory movements (inbound entries and outbound exits).
 */
public interface InventoryMovementService {

    /**
     * Registers a new stock movement (entry or exit), updating product inventory levels atomically.
     *
     * @param input the input data containing movement type, product, quantity, and reference details
     * @return the registered movement output details
     * @throws com.tekna.api.common.exception.ProductNotFoundException if the target product does not exist
     * @throws com.tekna.api.common.exception.InsufficientStockException if exiting more stock than available
     * @throws com.tekna.api.common.exception.NotFoundException if the supplier is not found
     */
    InventoryMovementOutput registerMovement(CreateInventoryMovementInput input);

    /**
     * Retrieves an inventory movement record by its unique identifier.
     *
     * @param id the movement UUID
     * @return the movement output details
     * @throws com.tekna.api.common.exception.NotFoundException if no movement exists with the given ID
     */
    InventoryMovementOutput getById(UUID id);

    /**
     * Performs a paginated search of inventory movements using dynamic filtering specifications.
     *
     * @param input the search parameters including page, size, sorting, and filter criteria
     * @return a paginated list of movement outputs
     */
    PageOutput<InventoryMovementOutput> search(SearchInput<InventoryMovementFilterInput> input);
}
