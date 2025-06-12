package com.github.jrohatsch.moqqa.data.impl;

import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.domain.Message;
import com.github.jrohatsch.moqqa.domain.PathListItem;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class DatahandlerMqtt implements Datahandler {
    private final MqttConnector mqttConnector;
    private final ConcurrentHashMap<String, Message> data;
    private final Set<String> monitoredTopics;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private ConcurrentLinkedQueue<Message> queue = new ConcurrentLinkedQueue<>();

    public DatahandlerMqtt(MqttConnector mqttConnector) {
        this.mqttConnector = mqttConnector;
        this.data = new ConcurrentHashMap<>();
        this.monitoredTopics = ConcurrentHashMap.newKeySet();

        // for each new message create a new thread to quickly save message in queue
        mqttConnector.setMessageConsumer(message -> {
            executorService.submit(()->queue.add(message));
        });

        // have one dedicated thread to read queue and insert to data map
        executorService.submit(() -> {
            while (true) {
                var message = queue.poll();
                if (message != null) {
                    data.put(message.topic(), message);
                }
            }
        });

    }

    @Override
    public boolean connect(String url) {
        return mqttConnector.connect(url);
    }

    @Override
    public Set<PathListItem> getPathItems(String path) {
        // clone one time
        var data = new ConcurrentHashMap<>(this.data);
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
            } catch (Exception ignored) {

            }
            return a.topic().compareTo(b.topic());
        });


        return new HashSet<>(output.stream().limit(Long.MAX_VALUE).toList());
    }

    @Override
    public void disconnect() {
        mqttConnector.disconnect();
        data.clear();
    }

    @Override
    public void forgetMonitoredValues() {
        monitoredTopics.clear();
    }

    @Override
    public void addToMonitoredValues(String path, String item) {
        path = path.endsWith("/") ? path : path + "/";
        int index = item.indexOf(" ");
        if (index != -1) {
            item = item.substring(0, index);
        }
        String key = path + item;
        if (data.containsKey(key)) {
            monitoredTopics.add(key);
        }
    }

    @Override
    public List<Message> getMonitoredValues() {
        return monitoredTopics.stream().map(data::get).collect(Collectors.toList());
    }

}
