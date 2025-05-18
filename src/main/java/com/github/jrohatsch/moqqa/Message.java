package com.github.jrohatsch.moqqa;

import java.time.Instant;

public record Message(String topic, String message, Instant time) {

}
