package com.tekna.api.controller;

import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;
import com.tekna.api.dto.CreateInventoryMovementInput;
import com.tekna.api.dto.InventoryMovementFilterInput;
import com.tekna.api.dto.InventoryMovementOutput;
import com.tekna.api.service.InventoryMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/inventory/movements")
@RequiredArgsConstructor
@Tag(name = "Inventory Movements", description = "Stock entries, exits and movement history endpoints")
public class InventoryMovementController {

    private final InventoryMovementService movementService;

    @PostMapping
    @Operation(summary = "Register an inventory movement (entry or exit)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Movement registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or insufficient stock")
    })
    public ResponseEntity<InventoryMovementOutput> create(@Valid @RequestBody CreateInventoryMovementInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movementService.registerMovement(input));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inventory movement by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movement retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Movement not found")
    })
    public ResponseEntity<InventoryMovementOutput> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(movementService.getById(id));
    }

    @PostMapping("/filter")
    @Operation(summary = "Search inventory movements with pagination and dynamic filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movements retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search filter parameters")
    })
    public ResponseEntity<PageOutput<InventoryMovementOutput>> search(@Valid @RequestBody SearchInput<InventoryMovementFilterInput> input) {
        return ResponseEntity.ok(movementService.search(input));
    }
}
