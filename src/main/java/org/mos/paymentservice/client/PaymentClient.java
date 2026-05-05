package org.mos.paymentservice.client;

import io.micrometer.core.instrument.Timer;
import org.mos.paymentservice.metricks.PaymentMetricks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.mos.paymentservice.stub.client.dto.PaymentDto;
import reactor.core.publisher.Mono;

@Component
public class PaymentClient {
    private static final Logger log = LoggerFactory.getLogger(PaymentClient.class);

    private final WebClient stubWebClient;
    private final PaymentMetricks metricks;

    public PaymentClient(WebClient stubWebClient, PaymentMetricks metricks) {
        this.stubWebClient = stubWebClient;
        this.metricks = metricks;
    }

    public Mono<PaymentDto> fetchPayment() {
        var sample = Timer.start();

        return stubWebClient.get()
                .uri("/payment")
                .retrieve()
                .bodyToMono(PaymentDto.class)
                .doOnNext(dto -> {
                    sample.stop(metricks.getFetchTimer());
                    metricks.incrementFetched();
                    log.debug("Fetched payment externalId={}", dto.getId());
                })
                .doOnError(e -> {
                    sample.stop(metricks.getFetchTimer());
                    metricks.incrementFetchError();
                    log.error("Failed to fetch from stub: {}", e.getMessage());
                });
    }
}
