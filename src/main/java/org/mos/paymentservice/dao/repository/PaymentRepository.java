package org.mos.paymentservice.dao.repository;

import org.mos.paymentservice.dao.entity.Payment;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface PaymentRepository extends ReactiveCrudRepository<Payment, Long> {

    @Query("SELECT * FROM payments ORDER BY saved_at DESC LIMIT :limit")
    Flux<Payment> findLatest(int limit);
}
