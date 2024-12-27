package com.study.orderservice.repository.user;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserInfoRepository extends ReactiveCrudRepository<UserEntity, String> {

    Mono<UserEntity> findDistinctFirstById(String id);

}
