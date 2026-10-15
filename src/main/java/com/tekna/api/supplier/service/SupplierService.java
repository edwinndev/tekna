package com.tekna.api.supplier.service;

import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;
import com.tekna.api.supplier.dto.CreateSupplierInput;
import com.tekna.api.supplier.dto.SupplierFilterInput;
import com.tekna.api.supplier.dto.SupplierOutput;
import com.tekna.api.supplier.dto.UpdateSupplierInput;

import java.util.UUID;

public interface SupplierService {

    SupplierOutput create(CreateSupplierInput input);

    SupplierOutput update(UpdateSupplierInput input);

    SupplierOutput getById(UUID id);

    PageOutput<SupplierOutput> search(SearchInput<SupplierFilterInput> input);

    void delete(UUID id);
}
