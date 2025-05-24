package com.github.jrohatsch.moqqa;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DataHandler {
    private final MqttConnector mqttConnector;
    private final ConcurrentHashMap<String, Message> data;
    private final Set<String> monitoredTopics;

    public DataHandler(MqttConnector mqttConnector) {
        this.mqttConnector = mqttConnector;
        this.data = new ConcurrentHashMap<>();
        this.monitoredTopics = ConcurrentHashMap.newKeySet();

        mqttConnector.setMessageConsumer(message -> {
            data.put(message.topic(), message);
        });
    }

    public boolean connect(String url) {
        return mqttConnector.connect(url);
    }

    public Set<PathListItem> getPathItems(String path) {
        List<PathListItem> output = new ArrayList<>();

        if (path == null || path.isBlank() || path.isEmpty()) {
            // return first level paths
            var entrySet = data.entrySet();
            for (var entry : entrySet) {
                int index = entry.getKey().indexOf("/");
                if (index == -1) {
                    output.add(new PathListItem(entry.getKey(), Optional.of(entry.getValue().message()), 0));
                } else {
                    long children = data.keySet().stream().filter(topic -> topic.startsWith(entry.getKey().substring(0, index))).count();
                    output.add(new PathListItem(entry.getKey().substring(0, index), Optional.empty(), children));
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
                        output.add(new PathListItem(pathItem, Optional.of(entry.getValue().message()), 0));
                    } else {
                        String finalPath = path;
                        long children = data.keySet().stream().filter(topic -> topic.startsWith(finalPath + pathItem.substring(0, index) + "/")).count();
                        output.add(new PathListItem(pathItem.substring(0, index), Optional.empty(), children));
                    }
                }
            }
        }

        output.sort((a, b) -> {
            try {
                int aInt = Integer.parseInt(a.topic());
                int bInt = Integer.parseInt(b.topic());
                return bInt - aInt;
            } catch (Exception e) {

            }
            return a.topic().compareTo(b.topic());
        });

        return new HashSet<>(output);
    }

    public void disconnect() {
        mqttConnector.disconnect();
        data.clear();
    }

    public void forgetMonitoredValues() {
        monitoredTopics.clear();
    }

    public boolean addToMonitoredValues(String path, String item) {
        path = path.endsWith("/") ? path : path + "/";
        int index = item.indexOf(" ");
        if (index != -1) {
            item = item.substring(0, index);
        }
        String key = path + item;
        if (data.containsKey(key)) {
            monitoredTopics.add(key);
            return true;
        } else {
            return false;
        }
    }

    public List<Message> getMonitoredValues() {
        return monitoredTopics.stream().map(topic -> data.get(topic)).collect(Collectors.toList());
    }

}
