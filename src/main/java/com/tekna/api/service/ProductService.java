package com.tekna.api.service;

import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;
import com.tekna.api.dto.CreateProductInput;
import com.tekna.api.dto.ProductFilterInput;
import com.tekna.api.dto.ProductOutput;
import com.tekna.api.dto.UpdateProductInput;

import java.util.UUID;

public interface ProductService {

    ProductOutput create(CreateProductInput input);

    ProductOutput update(UpdateProductInput input);

    ProductOutput getById(UUID id);

    ProductOutput getBySku(String sku);

    PageOutput<ProductOutput> search(SearchInput<ProductFilterInput> input);

    void delete(UUID id);
}
