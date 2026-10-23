package com.tekna.api.mapper;

import com.tekna.api.dto.RegisterInput;
import com.tekna.api.dto.UserOutput;
import com.tekna.api.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    User toEntity(RegisterInput input);

    UserOutput toOutput(User entity);

    List<UserOutput> toOutputList(List<User> entities);
}
