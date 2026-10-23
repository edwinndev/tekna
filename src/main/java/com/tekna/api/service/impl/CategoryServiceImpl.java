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
import com.tekna.api.service.CategoryService;
import com.tekna.api.specification.CategorySpecification;
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
        category.setCreatedBy(UserContext.getUserId());

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
        category.setUpdatedBy(UserContext.getUserId());

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
        category.setDeletedBy(UserContext.getUserId());
        categoryRepository.save(category);
    }
}
