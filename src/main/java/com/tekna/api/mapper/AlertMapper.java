package com.tekna.api.mapper;

import com.tekna.api.dto.AlertOutput;
import com.tekna.api.entity.Alert;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AlertMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    AlertOutput toOutput(Alert entity);

    List<AlertOutput> toOutputList(List<Alert> entities);
}
