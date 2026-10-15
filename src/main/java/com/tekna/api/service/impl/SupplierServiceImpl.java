package com.tekna.api.service.impl;

import com.tekna.api.common.exception.NotFoundException;
import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;
import com.tekna.api.dto.CreateSupplierInput;
import com.tekna.api.dto.SupplierFilterInput;
import com.tekna.api.dto.SupplierOutput;
import com.tekna.api.dto.UpdateSupplierInput;
import com.tekna.api.entity.Supplier;
import com.tekna.api.mapper.SupplierMapper;
import com.tekna.api.repository.SupplierRepository;
import com.tekna.api.service.SupplierService;
import com.tekna.api.specification.SupplierSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierServiceImpl implements SupplierService {

    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    @Transactional
    public SupplierOutput create(CreateSupplierInput input) {
        log.info("Creating supplier with name: {}", input.getName());

        Supplier supplier = supplierMapper.toEntity(input);
        supplier.setCreatedBy(SYSTEM_USER_ID);

        Supplier savedSupplier = supplierRepository.save(supplier);
        return supplierMapper.toOutput(savedSupplier);
    }

    @Override
    @Transactional
    public SupplierOutput update(UpdateSupplierInput input) {
        log.info("Updating supplier with id: {}", input.getId());

        Supplier supplier = supplierRepository.findById(input.getId())
                .orElseThrow(() -> new NotFoundException("Supplier not found with id: " + input.getId()));

        supplierMapper.updateEntityFromInput(input, supplier);
        supplier.setUpdatedBy(SYSTEM_USER_ID);

        Supplier updatedSupplier = supplierRepository.save(supplier);
        return supplierMapper.toOutput(updatedSupplier);
    }

    @Override
    public SupplierOutput getById(UUID id) {
        return supplierRepository.findById(id)
                .map(supplierMapper::toOutput)
                .orElseThrow(() -> new NotFoundException("Supplier not found with id: " + id));
    }

    @Override
    public PageOutput<SupplierOutput> search(SearchInput<SupplierFilterInput> input) {
        Sort sort = Sort.by(Sort.Direction.fromString(input.getSortDirection()), input.getSortBy());
        Pageable pageable = PageRequest.of(input.getPageNumber(), input.getPageSize(), sort);
        Specification<Supplier> spec = SupplierSpecification.withFilter(input.getFilter());

        Page<Supplier> page = supplierRepository.findAll(spec, pageable);
        return new PageOutput<>(
                supplierMapper.toOutputList(page.getContent()),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getSize(),
                page.getNumber()
        );
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Soft deleting supplier with id: {}", id);

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier not found with id: " + id));

        supplier.setDeletedAt(Instant.now());
        supplier.setDeletedBy(SYSTEM_USER_ID);
        supplierRepository.save(supplier);
    }
}
