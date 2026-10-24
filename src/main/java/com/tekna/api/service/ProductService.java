package com.tekna.api.service;

import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;
import com.tekna.api.dto.CreateProductInput;
import com.tekna.api.dto.ProductFilterInput;
import com.tekna.api.dto.ProductOutput;
import com.tekna.api.dto.UpdateProductInput;

import java.util.UUID;

/**
 * Service contract for managing peripheral and hardware products.
 */
public interface ProductService {

    /**
     * Creates and registers a new product in the catalog.
     *
     * @param input the product creation payload
     * @return the created product output
     * @throws com.tekna.api.common.exception.BadRequestException if a product with the same SKU already exists
     * @throws com.tekna.api.common.exception.NotFoundException if the specified category is not found
     */
    ProductOutput create(CreateProductInput input);

    /**
     * Updates an existing product's attributes.
     *
     * @param input the update payload containing product ID and updated fields
     * @return the updated product output
     * @throws com.tekna.api.common.exception.NotFoundException if the product or category is not found
     * @throws com.tekna.api.common.exception.BadRequestException if the updated SKU conflicts with another product
     */
    ProductOutput update(UpdateProductInput input);

    /**
     * Retrieves a product by its unique identifier.
     *
     * @param id the product UUID
     * @return the product output
     * @throws com.tekna.api.common.exception.NotFoundException if no product exists with the given ID
     */
    ProductOutput getById(UUID id);

    /**
     * Retrieves a product by its unique SKU code.
     *
     * @param sku the product SKU
     * @return the product output
     * @throws com.tekna.api.common.exception.NotFoundException if no product exists with the given SKU
     */
    ProductOutput getBySku(String sku);

    /**
     * Performs a paginated search of products matching dynamic filtering criteria.
     *
     * @param input the search parameters including page, size, sorting, and filter specifications
     * @return a paginated page of product outputs
     */
    PageOutput<ProductOutput> search(SearchInput<ProductFilterInput> input);

    /**
     * Performs a soft delete on a product by setting its deletion audit timestamp.
     *
     * @param id the product UUID to delete
     * @throws com.tekna.api.common.exception.NotFoundException if the product is not found
     */
    void delete(UUID id);
}
