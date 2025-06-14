package com.github.jrohatsch.moqqa.data.impl;

import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.data.MqttConnector;
import com.github.jrohatsch.moqqa.domain.Message;
import com.github.jrohatsch.moqqa.domain.PathListItem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DatahandlerImpl implements Datahandler {
    private final MqttConnector mqttConnector;
    private final ConcurrentHashMap<String, Message> data;
    private final Set<String> monitoredTopics;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ConcurrentLinkedQueue<Message> queue = new ConcurrentLinkedQueue<>();

    public DatahandlerImpl(MqttConnector mqttConnector) {
        this.mqttConnector = mqttConnector;
        this.data = new ConcurrentHashMap<>();
        this.monitoredTopics = ConcurrentHashMap.newKeySet();

        // for each new message create a new thread to quickly save message in queue
        mqttConnector.setMessageConsumer(message -> {
            executorService.submit(() -> queue.add(message));
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
        final String pathWithSeparator;

        if (path == null || path.isBlank() || path.isEmpty()) {
            pathWithSeparator = "";
        } else if (path.endsWith("/")) {
            pathWithSeparator = path;
        } else {
            pathWithSeparator = path + "/";
        }

        Map<String, Long> messagesPerTopic = data.keySet().stream()
                .filter(topic -> topic.startsWith(pathWithSeparator))
                .collect(Collectors.groupingBy(topic -> {
                    topic = topic.replace(pathWithSeparator, "");
                    int separatorIndex = topic.indexOf("/");

                    if (separatorIndex == -1) {
                        return topic;
                    } else {
                        return topic.substring(0, separatorIndex);
                    }
                }, Collectors.counting()));

        messagesPerTopic.entrySet().stream().forEach(entry -> {
            if (!data.containsKey(pathWithSeparator + entry.getKey())) {
                // this is a parent topic
                output.add(new PathListItem(entry.getKey(), Optional.empty(), entry.getValue()));
            } else {
                // this is a real value, remove everything after /
                output.add(new PathListItem(entry.getKey(), Optional.of(data.get(pathWithSeparator + entry.getKey()).message()), 0));
            }
        });


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
