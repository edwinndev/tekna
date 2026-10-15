package com.tekna.api.category.service;

import com.tekna.api.category.dto.CategoryFilterInput;
import com.tekna.api.category.dto.CategoryOutput;
import com.tekna.api.category.dto.CreateCategoryInput;
import com.tekna.api.category.dto.UpdateCategoryInput;
import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;

import java.util.UUID;

public interface CategoryService {

    CategoryOutput create(CreateCategoryInput input);

    CategoryOutput update(UpdateCategoryInput input);

    CategoryOutput getById(UUID id);

    PageOutput<CategoryOutput> search(SearchInput<CategoryFilterInput> input);

    void delete(UUID id);
}
