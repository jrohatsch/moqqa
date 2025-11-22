package com.github.jrohatsch.moqqa.domain;

import java.time.Instant;

public record Message(String topic, String message, Instant time) {
    public static Message of(String topic, String message) {
        return new Message(topic, message, Instant.now());
    }
}
