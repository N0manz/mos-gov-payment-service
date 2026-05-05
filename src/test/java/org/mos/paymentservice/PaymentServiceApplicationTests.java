package org.mos.paymentservice;

import org.junit.jupiter.api.Test;
import org.mos.paymentservice.api.generated.dto.PaymentResponseDto;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentServiceApplicationTests {

    @LocalServerPort
    private int port;

    private WebClient webClient() {
        return WebClient.create("http://localhost:" + port);
    }

    @Test
    void contextLoads() {
    }

    @Test
    void fetchPayment_returnsValidPayment() {
        webClient().post()
                .uri("/api/v1/payments/fetch")
                .retrieve()
                .bodyToMono(PaymentResponseDto.class)
                .as(StepVerifier::create)
                .assertNext(payment -> {
                    assertThat(payment.getId()).isNotNull();
                    assertThat(payment.getExternalId()).isNotNull();
                    assertThat(payment.getFromAccount()).matches("^ACC-\\d{6}$");
                    assertThat(payment.getToAccount()).matches("^ACC-\\d{6}$");
                    assertThat(payment.getAmount()).isPositive();
                    assertThat(payment.getStatus()).isNotNull();
                    assertThat(payment.getCurrency()).isNotNull();
                    assertThat(payment.getSavedAt()).isNotNull();
                })
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void getPayments_returnsListWithLimit() {
        // Прогрев: гарантируем что в БД есть хотя бы одна запись
        webClient().post()
                .uri("/api/v1/payments/fetch")
                .retrieve()
                .bodyToMono(PaymentResponseDto.class)
                .block(Duration.ofSeconds(5));

        webClient().get()
                .uri("/api/v1/payments?limit=5")
                .retrieve()
                .bodyToFlux(PaymentResponseDto.class)
                .collectList()
                .as(StepVerifier::create)
                .assertNext(list -> {
                    assertThat(list).isNotEmpty();
                    assertThat(list.size()).isLessThanOrEqualTo(5);
                    // Проверяем сортировку: каждый следующий savedAt <= предыдущего
                    for (int i = 1; i < list.size(); i++) {
                        assertThat(list.get(i).getSavedAt())
                                .isBeforeOrEqualTo(list.get(i - 1).getSavedAt());
                    }
                })
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void fetchPayment_20ParallelRequests_completesNonBlocking() {
        webClient().post()
                .uri("/api/v1/payments/fetch")
                .retrieve()
                .bodyToMono(PaymentResponseDto.class)
                .block(Duration.ofSeconds(5));

        Flux<PaymentResponseDto> requests = Flux.range(0, 20)
                .flatMap(i -> webClient().post()
                        .uri("/api/v1/payments/fetch")
                        .retrieve()
                        .bodyToMono(PaymentResponseDto.class));

        StepVerifier.create(requests.collectList())
                .assertNext(list -> assertThat(list).hasSize(20))
                .expectComplete()
                .verify(Duration.ofMillis(2000));
    }
}
