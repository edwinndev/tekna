package com.tekna.api.supplier.controller;

import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;
import com.tekna.api.supplier.dto.CreateSupplierInput;
import com.tekna.api.supplier.dto.SupplierFilterInput;
import com.tekna.api.supplier.dto.SupplierOutput;
import com.tekna.api.supplier.dto.UpdateSupplierInput;
import com.tekna.api.supplier.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/suppliers")
@RequiredArgsConstructor
@Tag(name = "Suppliers", description = "Supplier management endpoints")
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    @Operation(summary = "Create a new supplier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Supplier created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    public ResponseEntity<SupplierOutput> create(@Valid @RequestBody CreateSupplierInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.create(input));
    }

    @PutMapping
    @Operation(summary = "Update an existing supplier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Supplier updated successfully"),
            @ApiResponse(responseCode = "404", description = "Supplier not found")
    })
    public ResponseEntity<SupplierOutput> update(@Valid @RequestBody UpdateSupplierInput input) {
        return ResponseEntity.ok(supplierService.update(input));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Supplier retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Supplier not found")
    })
    public ResponseEntity<SupplierOutput> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(supplierService.getById(id));
    }

    @PostMapping("/filter")
    @Operation(summary = "Search suppliers with pagination and dynamic filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Suppliers retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search filter parameters")
    })
    public ResponseEntity<PageOutput<SupplierOutput>> search(@Valid @RequestBody SearchInput<SupplierFilterInput> input) {
        return ResponseEntity.ok(supplierService.search(input));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete supplier by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Supplier deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Supplier not found")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
