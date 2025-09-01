package com.mybank.atmweb.service;

import com.mybank.atmweb.domain.Idempotency;
import com.mybank.atmweb.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IdempotencyService {
    private final IdempotencyRepository repo;

    public Idempotency registerOrGet(String key) {
        return repo.findById(key)
                .orElseGet(() -> repo.save(new Idempotency(key, null, LocalDateTime.now())));
    }

    //트랜잭션 생성 후
    public void attachTxId(String key, Long txId) {
        Idempotency entity = repo.findById(key)
                .orElseThrow(() -> new IllegalArgumentException("Idempotency key not found: " + key));
        if (entity.getTxId() != null) {
            return;
        }

        entity.setTxId(txId);
        repo.save(entity);
    }
}
