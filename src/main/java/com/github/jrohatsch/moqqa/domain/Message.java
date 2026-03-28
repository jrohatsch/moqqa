package com.github.jrohatsch.moqqa.domain;

import java.time.Instant;

public record Message(String topic, String message, Instant time, boolean isRetained) {
    public static Message of(String topic, String message, boolean isRetained) {
        return new Message(topic, message, Instant.now(), isRetained);
    }
}
