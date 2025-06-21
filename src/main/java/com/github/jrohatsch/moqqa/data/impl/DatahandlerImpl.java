package com.github.jrohatsch.moqqa.data.impl;

import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.data.MqttConnector;
import com.github.jrohatsch.moqqa.data.PathObserver;
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

    private final List<PathObserver> pathObservers = new ArrayList<>();
    private String searchPath = "";
    private String selectedItem = "";

    public DatahandlerImpl(MqttConnector mqttConnector) {
        this.mqttConnector = mqttConnector;
        this.data = new ConcurrentHashMap<>();
        this.monitoredTopics = ConcurrentHashMap.newKeySet();

        // for each new message create a new thread to quickly save message in the queue
        mqttConnector.setMessageConsumer(message -> executorService.submit(() -> queue.add(message)));

        // have one dedicated thread to read the queue and insert to data map
        executorService.submit(() -> {
            while (true) {
                var message = queue.poll();
                if (message != null) {
                    data.put(message.topic(), message);
                }
            }
        });

    }

    public String getSearchPath() {
        return searchPath;
    }

    public void setSearchPath(String searchPath) {
        System.out.println("set search path to " + searchPath);

        this.searchPath = searchPath;
        pathObservers.forEach(observer -> observer.updatePath(getSearchPath()));
    }

    @Override
    public void registerPathObserver(PathObserver observer) {
        pathObservers.add(observer);
    }

    public String getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(String selectedItem) {
        System.out.println("set selected item to " + selectedItem);
        this.selectedItem = selectedItem;
    }

    @Override
    public void forgetMonitoredValue(String topic) {
        monitoredTopics.remove(topic);
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

        messagesPerTopic.forEach((key, value) -> {
            if (!data.containsKey(pathWithSeparator + key)) {
                // this is a parent topic
                output.add(new PathListItem(key, Optional.empty(), value));
            } else {
                // this is a real value, remove everything after /
                output.add(new PathListItem(key, Optional.of(data.get(pathWithSeparator + key).message()), 0));
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
    public void monitorSelection() {
        addToMonitoredValues(getSearchPath(), getSelectedItem());
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
