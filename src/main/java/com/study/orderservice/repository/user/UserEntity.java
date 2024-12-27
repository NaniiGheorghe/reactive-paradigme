package com.study.orderservice.repository.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@Document("users")
public class UserEntity {
    @Id
    private String id;
    private String name;
    private String phone;
}
