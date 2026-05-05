package org.mos.paymentservice.api.controller;

import org.mos.paymentservice.api.generated.PaymentApi;
import org.mos.paymentservice.api.generated.dto.PaymentResponseDto;
import org.mos.paymentservice.dao.mapper.PaymentMapper;
import org.mos.paymentservice.service.Payment.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class PaymentController implements PaymentApi {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;
    public PaymentController(PaymentService paymentService,
                             PaymentMapper paymentMapper) {

        this.paymentService = paymentService;
        this.paymentMapper = paymentMapper;

    }

    @Override
    public Mono<ResponseEntity<PaymentResponseDto>> fetchPayment(ServerWebExchange exchange) {
        return paymentService.fetchAndSave()
                .map(paymentMapper::toResponseDto)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Flux<PaymentResponseDto>>> getPayments(
            Integer limit, ServerWebExchange exchange) {
        Flux<PaymentResponseDto> flux = paymentService.getLatest(limit)
                .map(paymentMapper::toResponseDto);
        return Mono.just(ResponseEntity.ok(flux));
    }
}
