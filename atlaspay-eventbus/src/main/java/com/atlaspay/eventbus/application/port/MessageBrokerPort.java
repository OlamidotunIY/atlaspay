package com.atlaspay.eventbus.application.port;

public interface MessageBrokerPort {
    void send(String topic, String key, String payload);
}
