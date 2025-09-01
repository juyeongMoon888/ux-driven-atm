package com.mybank.atmweb.repository;

import com.mybank.atmweb.domain.Idempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface IdempotencyRepository extends JpaRepository<Idempotency, String> {
    Optional<Idempotency> findByKey(String key);

    boolean existByKey(String key);
}
