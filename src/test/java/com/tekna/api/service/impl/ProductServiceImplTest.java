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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private UUID productId;
    private UUID categoryId;
    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        category = new Category();
        category.setId(categoryId);
        category.setName("Gaming Peripherals");

        product = new Product();
        product.setId(productId);
        product.setSku("KB-RZR-HUNT");
        product.setName("Razer Huntsman Mini");
        product.setPrice(BigDecimal.valueOf(499.00));
        product.setStock(10);
        product.setMinStock(5);
        product.setCategory(category);
    }

    @Test
    @DisplayName("Should create product successfully when input is valid")
    void create_whenValidInput_shouldSaveAndReturnProductOutput() {
        CreateProductInput input = new CreateProductInput();
        input.setSku("KB-RZR-HUNT");
        input.setName("Razer Huntsman Mini");
        input.setCategoryId(categoryId);
        input.setPrice(BigDecimal.valueOf(499.00));
        input.setStock(10);
        input.setMinStock(5);

        Product mappedProduct = new Product();
        Product savedProduct = new Product();
        ProductOutput expectedOutput = new ProductOutput();
        expectedOutput.setId(productId);
        expectedOutput.setSku("KB-RZR-HUNT");

        when(productRepository.existsBySkuIgnoreCase("KB-RZR-HUNT")).thenReturn(false);
        when(productMapper.toEntity(input)).thenReturn(mappedProduct);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.save(mappedProduct)).thenReturn(savedProduct);
        when(productMapper.toOutput(savedProduct)).thenReturn(expectedOutput);

        ProductOutput result = productService.create(input);

        assertThat(result).isNotNull();
        assertThat(result.getSku()).isEqualTo("KB-RZR-HUNT");
        verify(productRepository).save(mappedProduct);
    }

    @Test
    @DisplayName("Should throw BadRequestException when SKU already exists upon creation")
    void create_whenSkuAlreadyExists_shouldThrowBadRequestException() {
        CreateProductInput input = new CreateProductInput();
        input.setSku("KB-RZR-HUNT");

        when(productRepository.existsBySkuIgnoreCase("KB-RZR-HUNT")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(input))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw NotFoundException when category is not found upon creation")
    void create_whenCategoryNotFound_shouldThrowNotFoundException() {
        CreateProductInput input = new CreateProductInput();
        input.setSku("KB-RZR-HUNT");
        input.setCategoryId(categoryId);

        Product mappedProduct = new Product();
        when(productRepository.existsBySkuIgnoreCase("KB-RZR-HUNT")).thenReturn(false);
        when(productMapper.toEntity(input)).thenReturn(mappedProduct);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(input))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Category not found with id");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update product successfully when input is valid")
    void update_whenValidInput_shouldUpdateAndReturnProductOutput() {
        UpdateProductInput input = new UpdateProductInput();
        input.setId(productId);
        input.setSku("KB-RZR-HUNT-V2");
        input.setName("Razer Huntsman Mini V2");
        input.setCategoryId(categoryId);
        input.setPrice(BigDecimal.valueOf(529.00));
        input.setStock(8);
        input.setMinStock(4);

        Product updatedProduct = new Product();
        ProductOutput expectedOutput = new ProductOutput();
        expectedOutput.setName("Razer Huntsman Mini V2");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.existsBySkuIgnoreCaseAndIdNot("KB-RZR-HUNT-V2", productId)).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.save(product)).thenReturn(updatedProduct);
        when(productMapper.toOutput(updatedProduct)).thenReturn(expectedOutput);

        ProductOutput result = productService.update(input);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Razer Huntsman Mini V2");
        verify(productMapper).updateEntityFromInput(input, product);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should throw NotFoundException when updating non-existent product")
    void update_whenProductNotFound_shouldThrowNotFoundException() {
        UpdateProductInput input = new UpdateProductInput();
        input.setId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(input))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Product not found with id");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BadRequestException when updating with SKU belonging to another product")
    void update_whenDuplicateSku_shouldThrowBadRequestException() {
        UpdateProductInput input = new UpdateProductInput();
        input.setId(productId);
        input.setSku("MOU-LOG-G502");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.existsBySkuIgnoreCaseAndIdNot("MOU-LOG-G502", productId)).thenReturn(true);

        assertThatThrownBy(() -> productService.update(input))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Another product with SKU");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get product by ID when product exists")
    void getById_whenProductExists_shouldReturnProductOutput() {
        ProductOutput expectedOutput = new ProductOutput();
        expectedOutput.setId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toOutput(product)).thenReturn(expectedOutput);

        ProductOutput result = productService.getById(productId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
    }

    @Test
    @DisplayName("Should throw NotFoundException when getting product by non-existent ID")
    void getById_whenProductNotFound_shouldThrowNotFoundException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(productId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Product not found with id");
    }

    @Test
    @DisplayName("Should get product by SKU when product exists")
    void getBySku_whenProductExists_shouldReturnProductOutput() {
        String sku = "KB-RZR-HUNT";
        ProductOutput expectedOutput = new ProductOutput();
        expectedOutput.setSku(sku);

        when(productRepository.findBySkuIgnoreCase(sku)).thenReturn(Optional.of(product));
        when(productMapper.toOutput(product)).thenReturn(expectedOutput);

        ProductOutput result = productService.getBySku(sku);

        assertThat(result).isNotNull();
        assertThat(result.getSku()).isEqualTo(sku);
    }

    @Test
    @DisplayName("Should soft delete product by setting deletedAt timestamp")
    void delete_whenProductExists_shouldSoftDeleteSuccessfully() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        productService.delete(productId);

        assertThat(product.getDeletedAt()).isNotNull();
        assertThat(product.getDeletedBy()).isNotNull();
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should return paged products when searching with filter")
    void search_shouldReturnPageOutput() {
        SearchInput<ProductFilterInput> input = new SearchInput<>();
        input.setPageNumber(0);
        input.setPageSize(10);
        input.setSortBy("name");
        input.setSortDirection("ASC");

        Page<Product> page = new PageImpl<>(List.of(product));
        ProductOutput output = new ProductOutput();

        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(productMapper.toOutputList(List.of(product))).thenReturn(List.of(output));

        PageOutput<ProductOutput> result = productService.search(input);

        assertThat(result).isNotNull();
        assertThat(result.getCount()).isEqualTo(1);
        assertThat(result.getTotal()).isEqualTo(1);
    }
}
