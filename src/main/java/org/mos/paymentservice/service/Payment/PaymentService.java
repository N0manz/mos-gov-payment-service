package org.mos.paymentservice.service.Payment;

import org.mos.paymentservice.client.PaymentClient;
import org.mos.paymentservice.dao.entity.Payment;
import org.mos.paymentservice.dao.mapper.PaymentMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Service
public class PaymentService {

    private final PaymentClient paymentClient;
    private final PaymentDaoService paymentDaoService;
    private final PaymentMapper paymentMapper;

    public PaymentService(PaymentClient paymentClient,
                          PaymentDaoService paymentDaoService,
                          PaymentMapper paymentMapper) {
        this.paymentClient = paymentClient;
        this.paymentDaoService = paymentDaoService;
        this.paymentMapper = paymentMapper;
    }

    public Mono<Payment> fetchAndSave() {
        return paymentClient.fetchPayment()
                .map(paymentMapper::toEntity)
                .flatMap(paymentDaoService::save)
                .timeout(Duration.ofSeconds(10));
    }

    public Flux<Payment> getLatest(int limit) {
        return paymentDaoService.findLatest(limit);
    }
}