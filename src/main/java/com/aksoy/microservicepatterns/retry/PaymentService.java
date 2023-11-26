package com.aksoy.microservicepatterns.retry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

/**
 * Simple payment service to demonstrate retryable pattern
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final BalanceService balanceService;

    @Retryable(retryFor = {
            ConnectTimeOutException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 1000, multiplier = 2),
            recover = "doPaymentRecover")
    public void doPayment(TransactionDto transactionDto) {
        //Assume that balanceService is another microservice
        balanceService.updateBalance(transactionDto);
        // additional operations related to do payment after updating balance
    }

    @Recover
    public void doPaymentRecover(TransactionDto transactionDto) {
        log.error("ConnectTimeoutException occurred while trying to do payment for transactionId: {}", transactionDto.transactionId());
        // Do some compensation operations if necessary
    }
}
