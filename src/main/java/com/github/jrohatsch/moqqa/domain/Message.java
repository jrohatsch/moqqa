package com.github.jrohatsch.moqqa.domain;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONParserConfiguration;

import java.time.Instant;
import java.util.Map;

public record Message(String topic, String message, Instant time) {
    public static Message of(String topic, String message) {
        return new Message(topic, message, Instant.now());
    }
}
