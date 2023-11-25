package com.aksoy.microservicepatterns.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class CircuitBreakerServiceTest {

    @SpyBean
    private CircuitBreakerService circuitBreakerService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp() {
        circuitBreakerRegistry.circuitBreaker(CircuitBreakerService.CB_PERFORM_TASK).reset();
    }

    @Test
    void test_circuit_breaker_is_closed() {
        String result = circuitBreakerService.performTask(false);

        assertEquals("Task completed successfully", result);
        verify(circuitBreakerService, never()).performTaskFallbackMethod(anyBoolean(), any());
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreakerRegistry.circuitBreaker(CircuitBreakerService.CB_PERFORM_TASK).getState());
    }

    @Test
    void test_circuit_breaker_is_open() {
        final int minNumOfCalls = 5;
        simulateFailuresForOpenState(minNumOfCalls);
        verify(circuitBreakerService, times(minNumOfCalls)).performTaskFallbackMethod(anyBoolean(), any());

        // One more failure should open the circuit breaker
        CallNotPermittedException openedCircuitBreaker = assertThrows(CallNotPermittedException.class, () -> circuitBreakerService.performTask(true));

        assertNotNull(openedCircuitBreaker);
        assertEquals("CircuitBreaker 'performTaskCircuitBreaker' is OPEN and does not permit further calls", openedCircuitBreaker.getMessage());
        assertNull(openedCircuitBreaker.getCause());
        assertEquals(CircuitBreaker.State.OPEN, circuitBreakerRegistry.circuitBreaker(CircuitBreakerService.CB_PERFORM_TASK).getState());
    }

    @Test
    void test_circuit_breaker_is_half_open() {
        final int minNumOfCalls = 5;
        final int durationInOpenState = 2500;
        simulateFailuresForOpenState(minNumOfCalls);
        verify(circuitBreakerService, times(minNumOfCalls)).performTaskFallbackMethod(anyBoolean(), any());
        assertThrows(CallNotPermittedException.class, () -> circuitBreakerService.performTask(true));

        // Wait for 2.5 sec to change state from OPEN to HALF-OPEN
        await().atMost(durationInOpenState, TimeUnit.MILLISECONDS)
                .until(
                        () -> circuitBreakerRegistry.circuitBreaker(CircuitBreakerService.CB_PERFORM_TASK).getState(),
                        equalTo(CircuitBreaker.State.HALF_OPEN));
    }

    // Simulate failures to open the circuit breaker
    private void simulateFailuresForOpenState(int minNumOfCalls) {
        IntStream.range(0, minNumOfCalls)
                .forEach(i -> assertDoesNotThrow(() -> circuitBreakerService.performTask(true)));
    }
}
