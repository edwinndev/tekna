package com.tekna.api.service.impl;

import com.tekna.api.common.exception.BadRequestException;
import com.tekna.api.common.exception.NotFoundException;
import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;
import com.tekna.api.dto.CategoryFilterInput;
import com.tekna.api.dto.CategoryOutput;
import com.tekna.api.dto.CreateCategoryInput;
import com.tekna.api.dto.UpdateCategoryInput;
import com.tekna.api.entity.Category;
import com.tekna.api.mapper.CategoryMapper;
import com.tekna.api.repository.CategoryRepository;
import com.tekna.api.security.UserContext;
import org.junit.jupiter.api.AfterEach;
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
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private UUID categoryId;
    private Category category;

    @BeforeEach
    void setUp() {
        UserContext.setUserId(UUID.randomUUID());
        categoryId = UUID.randomUUID();
        category = new Category();
        category.setId(categoryId);
        category.setName("Gaming Monitors");
        category.setDescription("High refresh rate gaming monitors");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("Should create category successfully when input is valid")
    void create_whenValidInput_shouldSaveAndReturnCategoryOutput() {
        CreateCategoryInput input = new CreateCategoryInput();
        input.setName("Gaming Monitors");
        input.setDescription("High refresh rate gaming monitors");

        Category mappedCategory = new Category();
        Category savedCategory = new Category();
        CategoryOutput expectedOutput = new CategoryOutput();
        expectedOutput.setId(categoryId);
        expectedOutput.setName("Gaming Monitors");

        when(categoryRepository.findByNameIgnoreCase("Gaming Monitors")).thenReturn(Optional.empty());
        when(categoryMapper.toEntity(input)).thenReturn(mappedCategory);
        when(categoryRepository.save(mappedCategory)).thenReturn(savedCategory);
        when(categoryMapper.toOutput(savedCategory)).thenReturn(expectedOutput);

        CategoryOutput result = categoryService.create(input);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Gaming Monitors");
        verify(categoryRepository).save(mappedCategory);
    }

    @Test
    @DisplayName("Should throw BadRequestException when creating category with duplicate name")
    void create_whenNameAlreadyExists_shouldThrowBadRequestException() {
        CreateCategoryInput input = new CreateCategoryInput();
        input.setName("Gaming Monitors");

        when(categoryRepository.findByNameIgnoreCase("Gaming Monitors")).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> categoryService.create(input))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update category successfully when input is valid")
    void update_whenValidInput_shouldUpdateAndReturnCategoryOutput() {
        UpdateCategoryInput input = new UpdateCategoryInput();
        input.setId(categoryId);
        input.setName("Pro Gaming Monitors");
        input.setDescription("Esports grade monitors");

        Category updatedCategory = new Category();
        CategoryOutput expectedOutput = new CategoryOutput();
        expectedOutput.setName("Pro Gaming Monitors");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Pro Gaming Monitors", categoryId)).thenReturn(false);
        when(categoryRepository.save(category)).thenReturn(updatedCategory);
        when(categoryMapper.toOutput(updatedCategory)).thenReturn(expectedOutput);

        CategoryOutput result = categoryService.update(input);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Pro Gaming Monitors");
        verify(categoryMapper).updateEntityFromInput(input, category);
        verify(categoryRepository).save(category);
    }

    @Test
    @DisplayName("Should throw NotFoundException when updating non-existent category")
    void update_whenCategoryNotFound_shouldThrowNotFoundException() {
        UpdateCategoryInput input = new UpdateCategoryInput();
        input.setId(categoryId);
        input.setName("Pro Gaming Monitors");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(input))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Category not found with id");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get category by ID when category exists")
    void getById_whenCategoryExists_shouldReturnCategoryOutput() {
        CategoryOutput expectedOutput = new CategoryOutput();
        expectedOutput.setId(categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryMapper.toOutput(category)).thenReturn(expectedOutput);

        CategoryOutput result = categoryService.getById(categoryId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
    }

    @Test
    @DisplayName("Should throw NotFoundException when getting category by non-existent ID")
    void getById_whenCategoryNotFound_shouldThrowNotFoundException() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(categoryId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Category not found with id");
    }

    @Test
    @DisplayName("Should soft delete category by setting deletedAt timestamp")
    void delete_whenCategoryExists_shouldSoftDeleteSuccessfully() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        categoryService.delete(categoryId);

        assertThat(category.getDeletedAt()).isNotNull();
        assertThat(category.getDeletedBy()).isNotNull();
        verify(categoryRepository).save(category);
    }

    @Test
    @DisplayName("Should return paged categories when searching with filter")
    void search_shouldReturnPageOutput() {
        SearchInput<CategoryFilterInput> input = new SearchInput<>();
        input.setPageNumber(0);
        input.setPageSize(10);
        input.setSortBy("name");
        input.setSortDirection("ASC");

        Page<Category> page = new PageImpl<>(List.of(category));
        CategoryOutput output = new CategoryOutput();

        when(categoryRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(categoryMapper.toOutputList(List.of(category))).thenReturn(List.of(output));

        PageOutput<CategoryOutput> result = categoryService.search(input);

        assertThat(result).isNotNull();
        assertThat(result.getCount()).isEqualTo(1);
        assertThat(result.getTotal()).isEqualTo(1);
    }
}
