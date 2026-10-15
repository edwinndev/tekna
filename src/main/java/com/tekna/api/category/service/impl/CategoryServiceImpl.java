package com.tekna.api.category.service.impl;

import com.tekna.api.category.dto.CategoryFilterInput;
import com.tekna.api.category.dto.CategoryOutput;
import com.tekna.api.category.dto.CreateCategoryInput;
import com.tekna.api.category.dto.UpdateCategoryInput;
import com.tekna.api.category.mapper.CategoryMapper;
import com.tekna.api.category.persistence.entity.Category;
import com.tekna.api.category.persistence.repository.CategoryRepository;
import com.tekna.api.category.persistence.specification.CategorySpecification;
import com.tekna.api.category.service.CategoryService;
import com.tekna.api.common.exception.BadRequestException;
import com.tekna.api.common.exception.NotFoundException;
import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;
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
public class CategoryServiceImpl implements CategoryService {

    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryOutput create(CreateCategoryInput input) {
        log.info("Creating category with name: {}", input.getName());

        categoryRepository.findByNameIgnoreCase(input.getName().trim()).ifPresent(existing -> {
            throw new BadRequestException("Category with name '" + input.getName() + "' already exists");
        });

        Category category = categoryMapper.toEntity(input);
        category.setName(input.getName().trim());
        category.setCreatedBy(SYSTEM_USER_ID);

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toOutput(savedCategory);
    }

    @Override
    @Transactional
    public CategoryOutput update(UpdateCategoryInput input) {
        log.info("Updating category with id: {}", input.getId());

        Category category = categoryRepository.findById(input.getId())
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + input.getId()));

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(input.getName().trim(), input.getId())) {
            throw new BadRequestException("Another category with name '" + input.getName() + "' already exists");
        }

        categoryMapper.updateEntityFromInput(input, category);
        category.setName(input.getName().trim());
        category.setUpdatedBy(SYSTEM_USER_ID);

        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toOutput(updatedCategory);
    }

    @Override
    public CategoryOutput getById(UUID id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toOutput)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
    }

    @Override
    public PageOutput<CategoryOutput> search(SearchInput<CategoryFilterInput> input) {
        Sort sort = Sort.by(Sort.Direction.fromString(input.getSortDirection()), input.getSortBy());
        Pageable pageable = PageRequest.of(input.getPageNumber(), input.getPageSize(), sort);
        Specification<Category> spec = CategorySpecification.withFilter(input.getFilter());

        Page<Category> page = categoryRepository.findAll(spec, pageable);
        return new PageOutput<>(
                categoryMapper.toOutputList(page.getContent()),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getSize(),
                page.getNumber()
        );
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Soft deleting category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));

        category.setDeletedAt(Instant.now());
        category.setDeletedBy(SYSTEM_USER_ID);
        categoryRepository.save(category);
    }
}
