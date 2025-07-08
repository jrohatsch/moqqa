package com.github.jrohatsch.moqqa.domain;

import java.util.Optional;

public record PathListItem(String topic, Optional<String> value, long childTopics) {
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(topic);

        if (value.isPresent()) {
            builder.append(" = ");
            builder.append(value.get());
        }

        if (childTopics > 1) {
            builder.append(" (%d child topics)".formatted(childTopics));
        } else if (childTopics == 1) {
            builder.append(" (1 child topic)");
        }

        return builder.toString();
    }
}
