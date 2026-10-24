package com.tekna.api.service.impl;

import com.tekna.api.common.exception.BadRequestException;
import com.tekna.api.common.exception.NotFoundException;
import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;
import com.tekna.api.dto.CreateInventoryMovementInput;
import com.tekna.api.dto.InventoryMovementFilterInput;
import com.tekna.api.dto.InventoryMovementOutput;
import com.tekna.api.entity.InventoryMovement;
import com.tekna.api.entity.MovementType;
import com.tekna.api.entity.Product;
import com.tekna.api.entity.Supplier;
import com.tekna.api.mapper.InventoryMovementMapper;
import com.tekna.api.repository.InventoryMovementRepository;
import com.tekna.api.repository.ProductRepository;
import com.tekna.api.repository.SupplierRepository;
import com.tekna.api.service.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryMovementServiceImplTest {

    @Mock
    private InventoryMovementRepository movementRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private InventoryMovementMapper movementMapper;

    @Mock
    private AlertService alertService;

    @InjectMocks
    private InventoryMovementServiceImpl movementService;

    private UUID productId;
    private UUID supplierId;
    private Product product;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        supplierId = UUID.randomUUID();

        product = new Product();
        product.setId(productId);
        product.setSku("MOU-LOG-G502");
        product.setName("Logitech G502 Hero");
        product.setStock(10);
        product.setMinStock(5);
        product.setPrice(BigDecimal.valueOf(250.00));

        supplier = new Supplier();
        supplier.setId(supplierId);
        supplier.setName("Logitech Distributor");
    }

    @Test
    @DisplayName("Should increase stock when an ENTRY movement is registered")
    void registerMovement_whenEntryMovement_shouldIncreaseStockSuccessfully() {
        CreateInventoryMovementInput input = new CreateInventoryMovementInput();
        input.setProductId(productId);
        input.setSupplierId(supplierId);
        input.setMovementType(MovementType.ENTRY);
        input.setQuantity(5);
        input.setUnitPrice(BigDecimal.valueOf(200.00));
        input.setDocumentNumber("F001-001");
        input.setReason("Purchase from distributor");

        InventoryMovement movement = new InventoryMovement();
        InventoryMovement savedMovement = new InventoryMovement();
        InventoryMovementOutput expectedOutput = new InventoryMovementOutput();
        expectedOutput.setNewStock(15);
        expectedOutput.setMovementType(MovementType.ENTRY);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(movementMapper.toEntity(input)).thenReturn(movement);
        when(movementRepository.save(movement)).thenReturn(savedMovement);
        when(movementMapper.toOutput(savedMovement)).thenReturn(expectedOutput);

        InventoryMovementOutput result = movementService.registerMovement(input);

        assertThat(result).isNotNull();
        assertThat(product.getStock()).isEqualTo(15);
        verify(productRepository).save(product);
        verify(movementRepository).save(movement);
    }

    @Test
    @DisplayName("Should decrease stock when an EXIT movement is registered with sufficient stock")
    void registerMovement_whenExitMovement_shouldDecreaseStockSuccessfully() {
        CreateInventoryMovementInput input = new CreateInventoryMovementInput();
        input.setProductId(productId);
        input.setMovementType(MovementType.EXIT);
        input.setQuantity(4);
        input.setDocumentNumber("B001-002");
        input.setReason("Store sale");

        InventoryMovement movement = new InventoryMovement();
        InventoryMovement savedMovement = new InventoryMovement();
        InventoryMovementOutput expectedOutput = new InventoryMovementOutput();
        expectedOutput.setNewStock(6);
        expectedOutput.setMovementType(MovementType.EXIT);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(movementMapper.toEntity(input)).thenReturn(movement);
        when(movementRepository.save(movement)).thenReturn(savedMovement);
        when(movementMapper.toOutput(savedMovement)).thenReturn(expectedOutput);

        InventoryMovementOutput result = movementService.registerMovement(input);

        assertThat(result).isNotNull();
        assertThat(product.getStock()).isEqualTo(6);
        verify(productRepository).save(product);
        verify(movementRepository).save(movement);
    }

    @Test
    @DisplayName("Should throw BadRequestException when EXIT movement exceeds current stock")
    void registerMovement_whenExitExceedsStock_shouldThrowBadRequestException() {
        CreateInventoryMovementInput input = new CreateInventoryMovementInput();
        input.setProductId(productId);
        input.setMovementType(MovementType.EXIT);
        input.setQuantity(20);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> movementService.registerMovement(input))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock for product");

        verify(movementRepository, never()).save(any());
        verify(productRepository, never()).save(product);
    }

    @Test
    @DisplayName("Should throw NotFoundException when product is not found")
    void registerMovement_whenProductNotFound_shouldThrowNotFoundException() {
        CreateInventoryMovementInput input = new CreateInventoryMovementInput();
        input.setProductId(productId);
        input.setMovementType(MovementType.ENTRY);
        input.setQuantity(5);

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movementService.registerMovement(input))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Product not found with id");

        verify(movementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw NotFoundException when supplier is not found for movement")
    void registerMovement_whenSupplierNotFound_shouldThrowNotFoundException() {
        CreateInventoryMovementInput input = new CreateInventoryMovementInput();
        input.setProductId(productId);
        input.setSupplierId(supplierId);
        input.setMovementType(MovementType.ENTRY);
        input.setQuantity(5);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movementService.registerMovement(input))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Supplier not found with id");

        verify(movementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve movement by ID successfully")
    void getById_whenMovementExists_shouldReturnMovementOutput() {
        UUID movementId = UUID.randomUUID();
        InventoryMovement movement = new InventoryMovement();
        InventoryMovementOutput output = new InventoryMovementOutput();
        output.setId(movementId);

        when(movementRepository.findById(movementId)).thenReturn(Optional.of(movement));
        when(movementMapper.toOutput(movement)).thenReturn(output);

        InventoryMovementOutput result = movementService.getById(movementId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(movementId);
    }

    @Test
    @DisplayName("Should throw NotFoundException when movement does not exist")
    void getById_whenMovementNotFound_shouldThrowNotFoundException() {
        UUID movementId = UUID.randomUUID();
        when(movementRepository.findById(movementId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movementService.getById(movementId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Inventory movement not found with id");
    }

    @Test
    @DisplayName("Should return paged inventory movements when searching with filter")
    void search_shouldReturnPageOutput() {
        SearchInput<InventoryMovementFilterInput> input = new SearchInput<>();
        input.setPageNumber(0);
        input.setPageSize(10);
        input.setSortBy("createdAt");
        input.setSortDirection("DESC");

        InventoryMovement movement = new InventoryMovement();
        InventoryMovementOutput output = new InventoryMovementOutput();
        Page<InventoryMovement> page = new PageImpl<>(List.of(movement));

        when(movementRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(movementMapper.toOutputList(List.of(movement))).thenReturn(List.of(output));

        PageOutput<InventoryMovementOutput> result = movementService.search(input);

        assertThat(result).isNotNull();
        assertThat(result.getCount()).isEqualTo(1);
        assertThat(result.getTotal()).isEqualTo(1);
    }
}
