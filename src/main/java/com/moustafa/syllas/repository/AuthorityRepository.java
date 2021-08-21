package com.moustafa.syllas.repository;

import com.moustafa.syllas.domain.Authority;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * Spring Data MongoDB repository for the {@link com.moustafa.syllas.domain.Authority} entity.
 */
public interface AuthorityRepository extends ReactiveMongoRepository<Authority, String> {
}
