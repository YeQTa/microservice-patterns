package com.aksoy.microservicepatterns.retry;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionDto(UUID transactionId, Long userId, BigDecimal amount) {
}
