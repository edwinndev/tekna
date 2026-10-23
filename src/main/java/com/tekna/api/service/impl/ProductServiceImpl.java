package com.tekna.api.service.impl;

import com.tekna.api.common.exception.BadRequestException;
import com.tekna.api.common.exception.NotFoundException;
import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;
import com.tekna.api.dto.CreateProductInput;
import com.tekna.api.dto.ProductFilterInput;
import com.tekna.api.dto.ProductOutput;
import com.tekna.api.dto.UpdateProductInput;
import com.tekna.api.entity.Category;
import com.tekna.api.entity.Product;
import com.tekna.api.mapper.ProductMapper;
import com.tekna.api.repository.CategoryRepository;
import com.tekna.api.repository.ProductRepository;
import com.tekna.api.security.UserContext;
import com.tekna.api.service.ProductService;
import com.tekna.api.specification.ProductSpecification;
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
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductOutput create(CreateProductInput input) {
        log.info("Creating product with SKU: {}", input.getSku());

        if (productRepository.existsBySkuIgnoreCase(input.getSku().trim())) {
            throw new BadRequestException("Product with SKU '" + input.getSku() + "' already exists");
        }

        Product product = productMapper.toEntity(input);
        product.setSku(input.getSku().trim());
        product.setCreatedBy(UserContext.getUserId());

        if (input.getCategoryId() != null) {
            Category category = categoryRepository.findById(input.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found with id: " + input.getCategoryId()));
            product.setCategory(category);
        }

        Product savedProduct = productRepository.save(product);
        return productMapper.toOutput(savedProduct);
    }

    @Override
    @Transactional
    public ProductOutput update(UpdateProductInput input) {
        log.info("Updating product with id: {}", input.getId());

        Product product = productRepository.findById(input.getId())
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + input.getId()));

        if (productRepository.existsBySkuIgnoreCaseAndIdNot(input.getSku().trim(), input.getId())) {
            throw new BadRequestException("Another product with SKU '" + input.getSku() + "' already exists");
        }

        productMapper.updateEntityFromInput(input, product);
        product.setSku(input.getSku().trim());
        product.setUpdatedBy(UserContext.getUserId());

        if (input.getCategoryId() != null) {
            Category category = categoryRepository.findById(input.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found with id: " + input.getCategoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        Product updatedProduct = productRepository.save(product);
        return productMapper.toOutput(updatedProduct);
    }

    @Override
    public ProductOutput getById(UUID id) {
        return productRepository.findById(id)
                .map(productMapper::toOutput)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
    }

    @Override
    public ProductOutput getBySku(String sku) {
        return productRepository.findBySkuIgnoreCase(sku.trim())
                .map(productMapper::toOutput)
                .orElseThrow(() -> new NotFoundException("Product not found with SKU: " + sku));
    }

    @Override
    public PageOutput<ProductOutput> search(SearchInput<ProductFilterInput> input) {
        Sort sort = Sort.by(Sort.Direction.fromString(input.getSortDirection()), input.getSortBy());
        Pageable pageable = PageRequest.of(input.getPageNumber(), input.getPageSize(), sort);
        Specification<Product> spec = ProductSpecification.withFilter(input.getFilter());

        Page<Product> page = productRepository.findAll(spec, pageable);
        return new PageOutput<>(
                productMapper.toOutputList(page.getContent()),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getSize(),
                page.getNumber()
        );
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Soft deleting product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));

        product.setDeletedAt(Instant.now());
        product.setDeletedBy(UserContext.getUserId());
        productRepository.save(product);
    }
}
