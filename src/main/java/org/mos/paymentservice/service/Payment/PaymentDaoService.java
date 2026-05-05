package org.mos.paymentservice.service.Payment;

import io.micrometer.core.instrument.Timer;
import org.mos.paymentservice.dao.entity.Payment;
import org.mos.paymentservice.dao.repository.PaymentRepository;
import org.mos.paymentservice.metricks.PaymentMetricks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PaymentDaoService {
    private static final Logger log = LoggerFactory.getLogger(PaymentDaoService.class);

    private final PaymentRepository repository;
    private final PaymentMetricks metricks;

    public PaymentDaoService(PaymentRepository repository, PaymentMetricks metricks) {
        this.repository = repository;
        this.metricks = metricks;
    }

    @Transactional
    public Mono<Payment> save(Payment payment) {
        var sample = Timer.start();

        return repository.save(payment)
                .doOnNext(saved -> {
                    sample.stop(metricks.getSaveTimer());
                    metricks.incrementSaved();
                    log.debug("Saved payment id={}", saved.id());
                })
                .doOnError(e -> {
                    sample.stop(metricks.getSaveTimer());
                    metricks.incrementSaveError();
                    log.error("Failed to save payment: {}", e.getMessage());
                });
    }

    @Transactional(readOnly = true)
    public Flux<Payment> findLatest(int limit) {
        var sample = Timer.start();

        return repository.findLatest(Math.clamp(limit, 1, 100))
                .doOnComplete(() -> {
                    sample.stop(metricks.getReadTimer());
                    metricks.incrementRead();
                });
    }
}
