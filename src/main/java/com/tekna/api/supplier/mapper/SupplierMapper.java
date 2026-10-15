package com.tekna.api.supplier.mapper;

import com.tekna.api.supplier.dto.CreateSupplierInput;
import com.tekna.api.supplier.dto.SupplierOutput;
import com.tekna.api.supplier.dto.UpdateSupplierInput;
import com.tekna.api.supplier.persistence.entity.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Supplier toEntity(CreateSupplierInput input);

    SupplierOutput toOutput(Supplier entity);

    List<SupplierOutput> toOutputList(List<Supplier> entities);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntityFromInput(UpdateSupplierInput input, @MappingTarget Supplier entity);
}
