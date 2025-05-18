package com.github.jrohatsch.moqqa;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DataHandler {
    private final MqttConnector mqttConnector;
    private final ConcurrentHashMap<String, Message> data;

    public DataHandler(MqttConnector mqttConnector) {
        this.mqttConnector = mqttConnector;
        this.data = new ConcurrentHashMap<>();

        mqttConnector.setMessageConsumer(message -> {
            data.put(message.topic(), message);
        });
    }

    public boolean connect(String url) {
        return mqttConnector.connect(url);
    }

    public List<String> getPathItems(String path) {
        List<String> output = new ArrayList<>();

        if (path == null || path.isBlank() || path.isEmpty()) {
            // return first level paths
            var entrySet = data.entrySet();
            for (var entry : entrySet) {
                int index = entry.getKey().indexOf("/");
                if (index == -1) {
                    output.add(entry.getKey() + " = " + entry.getValue().message());
                } else {
                    long children = data.keySet().stream().filter(topic -> topic.startsWith(entry.getKey().substring(0, index))).count();
                    output.add(entry.getKey().substring(0, index) + " (%d sub topics)".formatted(children));
                }
            }
        } else {
            path = path.endsWith("/") ? path : path + "/";

            for (var entry : data.entrySet()) {
                if (entry.getKey().startsWith(path)) {
                    // remove the path
                    String pathItem = entry.getKey().replace(path, "");

                    // ignore everything from next sub topics
                    int index = pathItem.indexOf("/");

                    if (index == -1) {
                        output.add(pathItem + " = " + entry.getValue().message());
                    } else {
                        String finalPath = path;
                        long children = data.keySet().stream().filter(topic -> topic.startsWith(finalPath + pathItem.substring(0, index) + "/")).count();
                        output.add(pathItem.substring(0, index) + " (%d sub topics)".formatted(children));
                    }
                }
            }
        }

        output.sort(String::compareTo);

        return output;
    }

    public void disconnect() {
        mqttConnector.disconnect();
        data.clear();
    }
}
