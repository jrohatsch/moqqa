package com.github.jrohatsch.moqqa.domain;

import java.util.Optional;

public record PathListItem(String topic, Optional<String> value, long childTopics) {
    @Override
    public String toString() {
        if (value.isEmpty()) {
            if (childTopics > 1) {
                return topic.concat(" (%d child topics)".formatted(childTopics));
            } else if (childTopics == 1) {
                return topic.concat(" (1 child topic)");
            }else {
                return topic;
            }
        } else {
            return topic.concat(" = ").concat(value.get());
        }
    }
}
