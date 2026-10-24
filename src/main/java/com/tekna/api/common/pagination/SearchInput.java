package com.tekna.api.common.pagination;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchInput<T> {

    @Min(value = 0, message = "Page number must be 0 or greater")
    private int pageNumber = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private int pageSize = 10;

    private String sortBy = "createdAt";
    private String sortDirection = "DESC";

    private T filter;
}
