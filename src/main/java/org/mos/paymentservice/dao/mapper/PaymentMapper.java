package org.mos.paymentservice.dao.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mos.paymentservice.api.generated.dto.PaymentResponseDto;
import org.mos.paymentservice.dao.entity.Payment;
import org.mos.paymentservice.stub.client.dto.PaymentDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

//окей я все таки сделал mapstruct
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {

    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "externalId",      source = "id",         qualifiedByName = "uuidToString")
    @Mapping(target = "currency",        source = "currency",   qualifiedByName = "currencyEnumToString")
    @Mapping(target = "status",          source = "status",     qualifiedByName = "statusEnumToString")
    @Mapping(target = "externalCreatedAt", source = "createdAt", qualifiedByName = "offsetDateTimeToInstant")
    @Mapping(target = "savedAt",         expression = "java(java.time.Instant.now())")
    Payment toEntity(PaymentDto dto);

    @Mapping(target = "externalId",      source = "externalId",      qualifiedByName = "stringToUuid")
    @Mapping(target = "currency",        source = "currency",         qualifiedByName = "stringToCurrencyEnum")
    @Mapping(target = "status",          source = "status",           qualifiedByName = "stringToStatusEnum")
    @Mapping(target = "externalCreatedAt", source = "externalCreatedAt", qualifiedByName = "instantToOffsetDateTime")
    @Mapping(target = "savedAt",         source = "savedAt",          qualifiedByName = "instantToOffsetDateTime")
    @Mapping(target = "amount",          source = "amount",           qualifiedByName = "bigDecimalToDouble")
    PaymentResponseDto toResponseDto(Payment payment);

    @Named("uuidToString")
    static String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    @Named("stringToUuid")
    static UUID stringToUuid(String s) {
        return s != null ? UUID.fromString(s) : null;
    }

    @Named("currencyEnumToString")
    static String currencyEnumToString(PaymentDto.CurrencyEnum e) {
        return e != null ? e.getValue() : null;
    }

    @Named("statusEnumToString")
    static String statusEnumToString(PaymentDto.StatusEnum e) {
        return e != null ? e.getValue() : null;
    }

    @Named("stringToCurrencyEnum")
    static PaymentResponseDto.CurrencyEnum stringToCurrencyEnum(String s) {
        return s != null ? PaymentResponseDto.CurrencyEnum.fromValue(s) : null;
    }

    @Named("stringToStatusEnum")
    static PaymentResponseDto.StatusEnum stringToStatusEnum(String s) {
        return s != null ? PaymentResponseDto.StatusEnum.fromValue(s) : null;
    }

    @Named("offsetDateTimeToInstant")
    static Instant offsetDateTimeToInstant(OffsetDateTime dt) {
        return dt != null ? dt.toInstant() : Instant.now();
    }

    @Named("instantToOffsetDateTime")
    static OffsetDateTime instantToOffsetDateTime(Instant instant) {
        return instant != null ? OffsetDateTime.ofInstant(instant, ZoneOffset.UTC) : null;
    }

    @Named("bigDecimalToDouble")
    static Double bigDecimalToDouble(BigDecimal bd) {
        return bd != null ? bd.doubleValue() : null;
    }
}
