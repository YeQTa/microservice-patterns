package com.aksoy.microservicepatterns.circuitbreaker;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CircuitBreakerService {

    public static final String CB_PERFORM_TASK = "performTaskCircuitBreaker";

    @CircuitBreaker(name = CB_PERFORM_TASK, fallbackMethod = "performTaskFallbackMethod")
    public String performTask(boolean shouldFail) {
        if (shouldFail) {
            throw new IllegalArgumentException("Error occurred.");
        }
        return "Task completed successfully";
    }

    public String performTaskFallbackMethod(boolean shouldFail, IllegalArgumentException t) {
        log.warn("Task is failed, shouldFail: {}", shouldFail);
        return t.getMessage();
    }
}
