package org.mos.paymentservice.service.Payment;

import io.micrometer.core.instrument.Timer;
import org.mos.paymentservice.client.PaymentClient;
import org.mos.paymentservice.dao.entity.Payment;
import org.mos.paymentservice.dao.repository.PaymentRepository;
import org.mos.paymentservice.metricks.PaymentMetricks;
import org.mos.paymentservice.util.PaymentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;
import org.mos.paymentservice.stub.client.dto.PaymentDto;

@Service
public class PaymentService {

    private final PaymentClient paymentClient;
    private final PaymentDaoService paymentDaoService;

    public PaymentService(PaymentClient paymentClient,
                          PaymentDaoService paymentDaoService) {
        this.paymentClient = paymentClient;
        this.paymentDaoService = paymentDaoService;
    }

    public Mono<Payment> fetchAndSave() {
        return paymentClient.fetchPayment()
                .map(PaymentUtil::toEntity)
                .flatMap(paymentDaoService::save)
                .timeout(Duration.ofSeconds(10));
    }

    public Flux<Payment> getLatest(int limit) {
        return paymentDaoService.findLatest(limit);
    }
}