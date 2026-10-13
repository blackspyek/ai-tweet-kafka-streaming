package com.microservices.demo.elastic.query.error.handler;

public class ElasticQueryServiceException extends RuntimeException {
    public ElasticQueryServiceException() {
    }
    public ElasticQueryServiceException(String message) {
        super(message);
    }
}
