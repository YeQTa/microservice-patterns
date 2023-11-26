package com.aksoy.microservicepatterns.retry;

public class ConnectTimeOutException extends RuntimeException {
    public ConnectTimeOutException() {
        super("Http connection time out occurred!");
    }
}
