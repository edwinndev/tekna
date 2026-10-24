package com.tekna.api.service.impl;

import com.tekna.api.dto.InventoryMovementFilterInput;
import com.tekna.api.dto.ProductFilterInput;
import com.tekna.api.entity.Category;
import com.tekna.api.entity.InventoryMovement;
import com.tekna.api.entity.MovementType;
import com.tekna.api.entity.Product;
import com.tekna.api.repository.InventoryMovementRepository;
import com.tekna.api.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryMovementRepository movementRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private Product product;
    private InventoryMovement movement;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setName("Periféricos");

        product = new Product();
        product.setId(UUID.randomUUID());
        product.setSku("MOU-LOG-G502");
        product.setName("Logitech G502 Hero");
        product.setCategory(category);
        product.setPrice(BigDecimal.valueOf(389.90));
        product.setStock(2);
        product.setMinStock(5);

        movement = new InventoryMovement();
        movement.setId(UUID.randomUUID());
        movement.setProduct(product);
        movement.setMovementType(MovementType.ENTRY);
        movement.setQuantity(10);
        movement.setPreviousStock(0);
        movement.setNewStock(10);
        movement.setUnitPrice(BigDecimal.valueOf(250.00));
        movement.setDocumentNumber("F001-001");
        movement.setReason("Lote inicial");
        movement.setCreatedAt(Instant.now());
    }

    @Test
    @DisplayName("Should generate valid Excel bytes for product inventory")
    void exportProductsToExcel_shouldGenerateValidExcel() {
        when(productRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(product));

        byte[] result = reportService.exportProductsToExcel(new ProductFilterInput());

        assertThat(result).isNotNull().isNotEmpty();
        // Check for standard ZIP (OOXML) file signature: PK (0x50, 0x4B)
        assertThat(result[0]).isEqualTo((byte) 0x50);
        assertThat(result[1]).isEqualTo((byte) 0x4B);
    }

    @Test
    @DisplayName("Should generate valid PDF bytes for product inventory")
    void exportProductsToPdf_shouldGenerateValidPdf() {
        when(productRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(product));

        byte[] result = reportService.exportProductsToPdf(new ProductFilterInput());

        assertThat(result).isNotNull().isNotEmpty();
        String header = new String(result, 0, Math.min(result.length, 5));
        assertThat(header).startsWith("%PDF");
    }

    @Test
    @DisplayName("Should generate valid Excel bytes for inventory movements")
    void exportMovementsToExcel_shouldGenerateValidExcel() {
        when(movementRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(movement));

        byte[] result = reportService.exportMovementsToExcel(new InventoryMovementFilterInput());

        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result[0]).isEqualTo((byte) 0x50);
        assertThat(result[1]).isEqualTo((byte) 0x4B);
    }

    @Test
    @DisplayName("Should generate valid PDF bytes for inventory movements")
    void exportMovementsToPdf_shouldGenerateValidPdf() {
        when(movementRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(movement));

        byte[] result = reportService.exportMovementsToPdf(new InventoryMovementFilterInput());

        assertThat(result).isNotNull().isNotEmpty();
        String header = new String(result, 0, Math.min(result.length, 5));
        assertThat(header).startsWith("%PDF");
    }
}
