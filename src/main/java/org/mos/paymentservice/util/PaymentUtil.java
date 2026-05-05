package org.mos.paymentservice.util;

import org.mos.paymentservice.dao.entity.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.mos.paymentservice.stub.client.dto.PaymentDto;
import org.mos.paymentservice.api.generated.dto.PaymentResponseDto;

// Можно было и mapstruct, но как-то протупил
public final class PaymentUtil {

    private PaymentUtil() {}

    public static Payment toEntity(PaymentDto dto) {
        return new Payment(
                null,
                dto.getId() != null ? dto.getId().toString() : null,
                dto.getFromAccount(),
                dto.getToAccount(),
                BigDecimal.valueOf(dto.getAmount()),
                dto.getCurrency() != null ? dto.getCurrency().getValue() : null,
                dto.getStatus() != null ? dto.getStatus().getValue() : null,
                dto.getDescription(),
                dto.getCreatedAt() != null ? dto.getCreatedAt().toInstant() : Instant.now(),
                Instant.now()
        );
    }

    public static PaymentResponseDto toResponseDto(Payment payment) {
        return new PaymentResponseDto()
                .id(payment.id())
                .externalId(payment.externalId() != null
                        ? java.util.UUID.fromString(payment.externalId()) : null)
                .fromAccount(payment.fromAccount())
                .toAccount(payment.toAccount())
                .amount(payment.amount() != null ? payment.amount().doubleValue() : null)
                .currency(payment.currency() != null
                        ? PaymentResponseDto.CurrencyEnum.fromValue(payment.currency()) : null)
                .status(payment.status() != null
                        ? PaymentResponseDto.StatusEnum.fromValue(payment.status()) : null)
                .description(payment.description())
                .externalCreatedAt(payment.externalCreatedAt() != null
                        ? OffsetDateTime.ofInstant(payment.externalCreatedAt(), ZoneOffset.UTC) : null)
                .savedAt(OffsetDateTime.ofInstant(payment.savedAt(), ZoneOffset.UTC));
    }
}
