package com.tekna.api.common.pagination;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageOutput<T> {
    private List<T> items;
    private int count;
    private long total;
    private int pageSize;
    private int pageNumber;
}
