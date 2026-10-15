package com.tekna.api.mapper;

import com.tekna.api.dto.CreateProductInput;
import com.tekna.api.dto.ProductOutput;
import com.tekna.api.dto.UpdateProductInput;
import com.tekna.api.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Product toEntity(CreateProductInput input);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "isLowStock", expression = "java(entity.getStock() != null && entity.getMinStock() != null && entity.getStock() <= entity.getMinStock())")
    ProductOutput toOutput(Product entity);

    List<ProductOutput> toOutputList(List<Product> entities);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntityFromInput(UpdateProductInput input, @MappingTarget Product entity);
}
