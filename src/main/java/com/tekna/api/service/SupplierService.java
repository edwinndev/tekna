package com.tekna.api.service;

import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;
import com.tekna.api.dto.CreateSupplierInput;
import com.tekna.api.dto.SupplierFilterInput;
import com.tekna.api.dto.SupplierOutput;
import com.tekna.api.dto.UpdateSupplierInput;

import java.util.UUID;

public interface SupplierService {

    SupplierOutput create(CreateSupplierInput input);

    SupplierOutput update(UpdateSupplierInput input);

    SupplierOutput getById(UUID id);

    PageOutput<SupplierOutput> search(SearchInput<SupplierFilterInput> input);

    void delete(UUID id);
}
