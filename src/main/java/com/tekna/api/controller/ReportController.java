package com.tekna.api.controller;

import com.tekna.api.dto.InventoryMovementFilterInput;
import com.tekna.api.dto.ProductFilterInput;
import com.tekna.api.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Inventory and movement report export endpoints")
public class ReportController {

    private static final String EXCEL_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ReportService reportService;

    @PostMapping("/products/excel")
    @Operation(summary = "Export product inventory report in Excel (.xlsx) format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Excel report generated successfully"),
            @ApiResponse(responseCode = "500", description = "Internal error generating Excel document")
    })
    public ResponseEntity<byte[]> exportProductsToExcel(@RequestBody(required = false) ProductFilterInput filter) {
        byte[] content = reportService.exportProductsToExcel(filter);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(EXCEL_MEDIA_TYPE));
        headers.setContentDisposition(ContentDisposition.attachment().filename("tekna_products_inventory.xlsx").build());
        return ResponseEntity.ok().headers(headers).body(content);
    }

    @PostMapping("/products/pdf")
    @Operation(summary = "Export product inventory report in PDF format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF report generated successfully"),
            @ApiResponse(responseCode = "500", description = "Internal error generating PDF document")
    })
    public ResponseEntity<byte[]> exportProductsToPdf(@RequestBody(required = false) ProductFilterInput filter) {
        byte[] content = reportService.exportProductsToPdf(filter);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("tekna_products_inventory.pdf").build());
        return ResponseEntity.ok().headers(headers).body(content);
    }

    @PostMapping("/movements/excel")
    @Operation(summary = "Export inventory movements report in Excel (.xlsx) format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Excel report generated successfully"),
            @ApiResponse(responseCode = "500", description = "Internal error generating Excel document")
    })
    public ResponseEntity<byte[]> exportMovementsToExcel(@RequestBody(required = false) InventoryMovementFilterInput filter) {
        byte[] content = reportService.exportMovementsToExcel(filter);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(EXCEL_MEDIA_TYPE));
        headers.setContentDisposition(ContentDisposition.attachment().filename("tekna_inventory_movements.xlsx").build());
        return ResponseEntity.ok().headers(headers).body(content);
    }

    @PostMapping("/movements/pdf")
    @Operation(summary = "Export inventory movements report in PDF format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF report generated successfully"),
            @ApiResponse(responseCode = "500", description = "Internal error generating PDF document")
    })
    public ResponseEntity<byte[]> exportMovementsToPdf(@RequestBody(required = false) InventoryMovementFilterInput filter) {
        byte[] content = reportService.exportMovementsToPdf(filter);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("tekna_inventory_movements.pdf").build());
        return ResponseEntity.ok().headers(headers).body(content);
    }
}
