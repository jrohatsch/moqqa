package com.github.jrohatsch.moqqa.domain;

import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

public record PathListItem(String topic, Optional<String> value, long childTopics) {

    public PathListItemType type() {
        if (childTopics() > 0) {
            return PathListItemType.PARENT;
        } else if (value().isPresent()) {
            String value = value().get();

            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                return PathListItemType.BOOL;
            }
            try {
                if (new ObjectMapper().readTree(value).isNumber()) {
                    return PathListItemType.NUMBER;
                }
            } catch (Exception ignored) {

            }
        }

        return PathListItemType.TEXT;
    }

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
