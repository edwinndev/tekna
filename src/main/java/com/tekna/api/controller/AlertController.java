package com.tekna.api.controller;

import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;
import com.tekna.api.dto.AlertFilterInput;
import com.tekna.api.dto.AlertOutput;
import com.tekna.api.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Low stock alert management endpoints")
public class AlertController {

    private final AlertService alertService;

    @GetMapping("/{id}")
    @Operation(summary = "Get alert by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alert retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Alert not found")
    })
    public ResponseEntity<AlertOutput> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(alertService.getById(id));
    }

    @PutMapping("/{id}/attend")
    @Operation(summary = "Mark alert as attended")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alert marked as attended successfully"),
            @ApiResponse(responseCode = "404", description = "Alert not found")
    })
    public ResponseEntity<AlertOutput> markAsAttended(@PathVariable UUID id) {
        return ResponseEntity.ok(alertService.markAsAttended(id));
    }

    @PostMapping("/filter")
    @Operation(summary = "Search alerts with pagination and dynamic filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alerts retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search filter parameters")
    })
    public ResponseEntity<PageOutput<AlertOutput>> search(@Valid @RequestBody SearchInput<AlertFilterInput> input) {
        return ResponseEntity.ok(alertService.search(input));
    }

    @PostMapping("/check")
    @Operation(summary = "Trigger manual check for low-stock products to generate alerts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock check triggered successfully"),
            @ApiResponse(responseCode = "500", description = "Internal error checking inventory stock")
    })
    public ResponseEntity<Map<String, Object>> checkStock() {
        int generated = alertService.checkAllProductsStockScheduled();
        return ResponseEntity.ok(Map.of("message", "Stock check completed", "generatedAlerts", generated));
    }
}
