package com.aksoy.microservicepatterns.retry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class BalanceService {

    private static final Set<UUID> transactionSet = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void updateBalance(TransactionDto transactionDto) {
        if (isTransactionNotProcessed(transactionDto.transactionId())) {
            transactionSet.add(transactionDto.transactionId());
            log.info("Balance is updated for transactionId: {}", transactionDto.transactionId());
        }
    }

    private static boolean isTransactionNotProcessed(UUID transactionId) {
        return !transactionSet.contains(transactionId);
    }
}
