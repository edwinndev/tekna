package com.tekna.api.product.service;

import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;
import com.tekna.api.product.dto.CreateProductInput;
import com.tekna.api.product.dto.ProductFilterInput;
import com.tekna.api.product.dto.ProductOutput;
import com.tekna.api.product.dto.UpdateProductInput;

import java.util.UUID;

public interface ProductService {

    ProductOutput create(CreateProductInput input);

    ProductOutput update(UpdateProductInput input);

    ProductOutput getById(UUID id);

    ProductOutput getBySku(String sku);

    PageOutput<ProductOutput> search(SearchInput<ProductFilterInput> input);

    void delete(UUID id);
}
