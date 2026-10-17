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
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierMapper supplierMapper;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private UUID supplierId;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        supplierId = UUID.randomUUID();
        supplier = new Supplier();
        supplier.setId(supplierId);
        supplier.setName("Asus Wholesale Peru");
        supplier.setContactName("Fernando Quispe");
        supplier.setEmail("ventas@asuswholesale.pe");
        supplier.setPhone("+51 998 877 665");
    }

    @Test
    @DisplayName("Should create supplier successfully when input is valid")
    void create_whenValidInput_shouldSaveAndReturnSupplierOutput() {
        CreateSupplierInput input = new CreateSupplierInput();
        input.setName("Asus Wholesale Peru");
        input.setContactName("Fernando Quispe");
        input.setEmail("ventas@asuswholesale.pe");
        input.setPhone("+51 998 877 665");

        Supplier mappedSupplier = new Supplier();
        Supplier savedSupplier = new Supplier();
        SupplierOutput expectedOutput = new SupplierOutput();
        expectedOutput.setId(supplierId);
        expectedOutput.setName("Asus Wholesale Peru");

        when(supplierMapper.toEntity(input)).thenReturn(mappedSupplier);
        when(supplierRepository.save(mappedSupplier)).thenReturn(savedSupplier);
        when(supplierMapper.toOutput(savedSupplier)).thenReturn(expectedOutput);

        SupplierOutput result = supplierService.create(input);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Asus Wholesale Peru");
        verify(supplierRepository).save(mappedSupplier);
    }

    @Test
    @DisplayName("Should update supplier successfully when input is valid")
    void update_whenValidInput_shouldUpdateAndReturnSupplierOutput() {
        UpdateSupplierInput input = new UpdateSupplierInput();
        input.setId(supplierId);
        input.setName("Asus Official Distributor");

        Supplier updatedSupplier = new Supplier();
        SupplierOutput expectedOutput = new SupplierOutput();
        expectedOutput.setName("Asus Official Distributor");

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(supplierRepository.save(supplier)).thenReturn(updatedSupplier);
        when(supplierMapper.toOutput(updatedSupplier)).thenReturn(expectedOutput);

        SupplierOutput result = supplierService.update(input);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Asus Official Distributor");
        verify(supplierMapper).updateEntityFromInput(input, supplier);
        verify(supplierRepository).save(supplier);
    }

    @Test
    @DisplayName("Should throw NotFoundException when updating non-existent supplier")
    void update_whenSupplierNotFound_shouldThrowNotFoundException() {
        UpdateSupplierInput input = new UpdateSupplierInput();
        input.setId(supplierId);
        input.setName("Asus Official Distributor");

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.update(input))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Supplier not found with id");

        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get supplier by ID when supplier exists")
    void getById_whenSupplierExists_shouldReturnSupplierOutput() {
        SupplierOutput expectedOutput = new SupplierOutput();
        expectedOutput.setId(supplierId);

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(supplierMapper.toOutput(supplier)).thenReturn(expectedOutput);

        SupplierOutput result = supplierService.getById(supplierId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(supplierId);
    }

    @Test
    @DisplayName("Should throw NotFoundException when getting supplier by non-existent ID")
    void getById_whenSupplierNotFound_shouldThrowNotFoundException() {
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.getById(supplierId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Supplier not found with id");
    }

    @Test
    @DisplayName("Should soft delete supplier by setting deletedAt timestamp")
    void delete_whenSupplierExists_shouldSoftDeleteSuccessfully() {
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));

        supplierService.delete(supplierId);

        assertThat(supplier.getDeletedAt()).isNotNull();
        assertThat(supplier.getDeletedBy()).isNotNull();
        verify(supplierRepository).save(supplier);
    }

    @Test
    @DisplayName("Should return paged suppliers when searching with filter")
    void search_shouldReturnPageOutput() {
        SearchInput<SupplierFilterInput> input = new SearchInput<>();
        input.setPageNumber(0);
        input.setPageSize(10);
        input.setSortBy("name");
        input.setSortDirection("ASC");

        Page<Supplier> page = new PageImpl<>(List.of(supplier));
        SupplierOutput output = new SupplierOutput();

        when(supplierRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(supplierMapper.toOutputList(List.of(supplier))).thenReturn(List.of(output));

        PageOutput<SupplierOutput> result = supplierService.search(input);

        assertThat(result).isNotNull();
        assertThat(result.getCount()).isEqualTo(1);
        assertThat(result.getTotal()).isEqualTo(1);
    }
}
