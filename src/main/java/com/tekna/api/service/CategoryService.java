package com.tekna.api.service;

import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;
import com.tekna.api.dto.CategoryFilterInput;
import com.tekna.api.dto.CategoryOutput;
import com.tekna.api.dto.CreateCategoryInput;
import com.tekna.api.dto.UpdateCategoryInput;

import java.util.UUID;

public interface CategoryService {

    CategoryOutput create(CreateCategoryInput input);

    CategoryOutput update(UpdateCategoryInput input);

    CategoryOutput getById(UUID id);

    PageOutput<CategoryOutput> search(SearchInput<CategoryFilterInput> input);

    void delete(UUID id);
}
