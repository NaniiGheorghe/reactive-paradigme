package com.study.orderservice.repository.user;


import com.study.orderservice.domain.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserEntityMapper {

    UserEntityMapper INSTANCE = Mappers.getMapper(UserEntityMapper.class);

    User toDomainModel(UserEntity userEntity);

}
