package org.mos.paymentservice.dao.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table("payments")
public record Payment(
        @Id Long id,
        String externalId,
        String fromAccount,
        String toAccount,
        BigDecimal amount,
        String currency,
        String status,
        String description,
        Instant externalCreatedAt,
        Instant savedAt
) {
}
