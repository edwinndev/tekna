package com.tekna.api.service.impl;

import com.tekna.api.common.exception.BadRequestException;
import com.tekna.api.common.exception.InsufficientStockException;
import com.tekna.api.common.exception.NotFoundException;
import com.tekna.api.common.exception.ProductNotFoundException;
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
import com.tekna.api.security.UserContext;
import com.tekna.api.service.AlertService;
import com.tekna.api.service.InventoryMovementService;
import com.tekna.api.specification.InventoryMovementSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryMovementServiceImpl implements InventoryMovementService {

    private final InventoryMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryMovementMapper movementMapper;
    private final AlertService alertService;

    @Override
    @Transactional
    public InventoryMovementOutput registerMovement(CreateInventoryMovementInput input) {
        log.info("Registering {} movement for product id: {}", input.getMovementType(), input.getProductId());

        Product product = productRepository.findById(input.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(input.getProductId()));

        Supplier supplier = null;
        if (input.getSupplierId() != null) {
            supplier = supplierRepository.findById(input.getSupplierId())
                    .orElseThrow(() -> new NotFoundException("Supplier not found with id: " + input.getSupplierId()));
        }

        int previousStock = product.getStock();
        input.getMovementType().applyToStock(product, input.getQuantity());
        int newStock = product.getStock();

        if (input.getMovementType() == MovementType.ENTRY) {
            log.info("Inbound stock updated. Product: {}, Previous: {}, New: {}", product.getSku(), previousStock, newStock);
        } else if (input.getMovementType() == MovementType.EXIT) {
            log.info("Outbound stock updated. Product: {}, Previous: {}, New: {}", product.getSku(), previousStock, newStock);

            if (product.isLowStock()) {
                alertService.checkAndGenerateAlert(product, newStock);
            }
        }

        product.setUpdatedBy(UserContext.getUserId());
        productRepository.save(product);

        InventoryMovement movement = movementMapper.toEntity(input);
        movement.setProduct(product);
        movement.setSupplier(supplier);
        movement.setPreviousStock(previousStock);
        movement.setNewStock(newStock);
        movement.setCreatedBy(UserContext.getUserId());

        InventoryMovement savedMovement = movementRepository.save(movement);
        return movementMapper.toOutput(savedMovement);
    }

    @Override
    public InventoryMovementOutput getById(UUID id) {
        return movementRepository.findById(id)
                .map(movementMapper::toOutput)
                .orElseThrow(() -> new NotFoundException("Inventory movement not found with id: " + id));
    }

    @Override
    public PageOutput<InventoryMovementOutput> search(SearchInput<InventoryMovementFilterInput> input) {
        Sort sort = Sort.by(Sort.Direction.fromString(input.getSortDirection()), input.getSortBy());
        Pageable pageable = PageRequest.of(input.getPageNumber(), input.getPageSize(), sort);
        Specification<InventoryMovement> spec = InventoryMovementSpecification.withFilter(input.getFilter());

        Page<InventoryMovement> page = movementRepository.findAll(spec, pageable);
        return new PageOutput<>(
                movementMapper.toOutputList(page.getContent()),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getSize(),
                page.getNumber()
        );
    }
}
