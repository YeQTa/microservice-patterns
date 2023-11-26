package com.aksoy.microservicepatterns.retry;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class PaymentServiceTest {
    @SpyBean
    PaymentService paymentService;
    @MockBean
    BalanceService balanceService;

    @Test
    void test_payment_is_successful_without_retrying() {
        // Given
        TransactionDto transactionDto = createTransactionDto();
        doCallRealMethod().when(balanceService).updateBalance(transactionDto);

        // When
        paymentService.doPayment(transactionDto);

        // Then
        verify(paymentService, never()).doPaymentRecover(transactionDto);
        verify(paymentService).doPayment(transactionDto);
        verify(balanceService).updateBalance(transactionDto);
    }

    @Test
    void test_doPayment_retries_5_times_to_reach_balance_service_and_gets_connection_exception_then_recover_method_is_called() {
        // Given
        TransactionDto transactionDto = createTransactionDto();
        doThrow(ConnectTimeOutException.class).when(balanceService).updateBalance(transactionDto);

        // When
        paymentService.doPayment(transactionDto);

        // Then
        verify(balanceService, times(5)).updateBalance(transactionDto);
        verify(paymentService, times(5)).doPayment(transactionDto);
        verify(paymentService).doPaymentRecover(transactionDto);
    }

    @Test
    void test_doPayment_retries_5_times_to_reach_balance_service_and_gets_connection_exception_4_times_then_returns_successfully() {
        // Given
        TransactionDto transactionDto = createTransactionDto();
        doThrow(ConnectTimeOutException.class)
                .doThrow(ConnectTimeOutException.class)
                .doThrow(ConnectTimeOutException.class)
                .doThrow(ConnectTimeOutException.class)
                .doCallRealMethod()
                .when(balanceService).updateBalance(transactionDto);

        // When
        paymentService.doPayment(transactionDto);

        // Then
        verify(balanceService, times(5)).updateBalance(transactionDto);
        verify(paymentService, times(5)).doPayment(transactionDto);
        verify(paymentService, never()).doPaymentRecover(transactionDto);
    }


    private TransactionDto createTransactionDto() {
        return new TransactionDto(UUID.randomUUID(), 1L, BigDecimal.ONE);
    }
}
