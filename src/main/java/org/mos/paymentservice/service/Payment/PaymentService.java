package org.mos.paymentservice.service.Payment;

import io.micrometer.core.instrument.Timer;
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

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final WebClient stubWebClient;
    private final PaymentRepository repository;
    private final PaymentMetricks metricks;

    public PaymentService(WebClient stubWebClient,
                          PaymentRepository repository,
                          PaymentMetricks metricks) {
        this.stubWebClient = stubWebClient;
        this.repository = repository;
        this.metricks = metricks;
    }

    public Mono<Payment> fetchAndSave() {
        var fetchSample = Timer.start();

        return stubWebClient.get()
                .uri("/payment")
                .retrieve()
                .bodyToMono(PaymentDto.class)
                .doOnNext(dto -> {
                    fetchSample.stop(metricks.getFetchTimer());
                    metricks.incrementFetched();
                    log.debug("Fetched payment externalId={}", dto.getId());
                })
                .doOnError(e -> {
                    fetchSample.stop(metricks.getFetchTimer());
                    metricks.incrementFetchError();
                    log.error("Failed to fetch from stub: {}", e.getMessage());
                })
                .map(PaymentUtil::toEntity)
                .flatMap(entity -> {
                    var saveSample = Timer.start();
                    return repository.save(entity)
                            .doOnNext(saved -> {
                                saveSample.stop(metricks.getSaveTimer());
                                metricks.incrementSaved();
                                log.debug("Saved payment id={}", saved.id());
                            })
                            .doOnError(e -> {
                                saveSample.stop(metricks.getSaveTimer());
                                metricks.incrementSaveError();
                                log.error("Failed to save payment: {}", e.getMessage());
                            });
                })
                .timeout(Duration.ofSeconds(10));
    }


    public Flux<Payment> getLatest(int limit) {
        var sample = Timer.start();
        return repository.findLatest(Math.clamp(limit, 1, 100))
                .doOnComplete(() -> {
                    sample.stop(metricks.getReadTimer());
                    metricks.incrementRead();
                });
    }
}