package com.moustafa.syllas.repository;

import com.moustafa.syllas.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Spring Data MongoDB repository for the {@link com.moustafa.syllas.domain.User} entity.
 */
@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {

    Mono<User> findOneByActivationKey(String activationKey);

    Flux<User> findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant dateTime);

    Mono<User> findOneByResetKey(String resetKey);

    Mono<User> findOneByEmailIgnoreCase(String email);

    Mono<User> findOneByLogin(String login);

    Mono<User> findOneByLoginIgnoreCase(String login);

    Flux<User> findAllByLoginNot(Pageable pageable, String login);

    Flux<User> findAllByLoginNotAndApikeyNull(Pageable pageable, String login);

    Mono<Long> countAllByLoginNot(String anonymousUser);
}
