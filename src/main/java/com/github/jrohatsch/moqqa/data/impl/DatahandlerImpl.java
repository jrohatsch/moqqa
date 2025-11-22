package com.github.jrohatsch.moqqa.data.impl;

import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.data.MqttConnector;
import com.github.jrohatsch.moqqa.data.PathObserver;
import com.github.jrohatsch.moqqa.data.SelectionObserver;
import com.github.jrohatsch.moqqa.domain.Message;
import com.github.jrohatsch.moqqa.domain.PathListItem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DatahandlerImpl implements Datahandler {
    private final MqttConnector mqttConnector;
    private final ConcurrentHashMap<String, Message> data;
    private final ConcurrentHashMap<String, LinkedList<Message>> monitored;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ConcurrentLinkedQueue<Message> queue = new ConcurrentLinkedQueue<>();
    private final List<PathObserver> pathObservers = new ArrayList<>();
    private final List<SelectionObserver> selectionObserver = new ArrayList<>();
    private Predicate<Message> searchFilter = message -> true;
    private String searchPath = "";
    private PathListItem selectedItem;

    public DatahandlerImpl(MqttConnector mqttConnector) {
        this.mqttConnector = mqttConnector;
        this.data = new ConcurrentHashMap<>();
        this.monitored = new ConcurrentHashMap<>();

        // for each new message create a new thread to quickly save message in the queue
        mqttConnector.setMessageConsumer(message -> executorService.submit(() -> queue.add(message)));

        // have one dedicated thread to read the queue and insert to data map
        executorService.submit(() -> {
            while (true) {
                if (queue.isEmpty()) {
                    continue;
                }
                var message = queue.poll();
                if (message != null) {
                    handleMessage(message);
                }
            }
        });

    }

    private void handleMessage(Message message) {
        if (message.message().equals("")) {
            data.remove(message.topic());
            return;
        }

        data.put(message.topic(), message);

        if (monitored.containsKey(message.topic())) {
            monitored.get(message.topic()).add(message);
        }
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
    public void setSearchFilter(Predicate<Message> filter) {
        searchFilter = filter;
    }

    @Override
    public void registerPathObserver(PathObserver observer) {
        pathObservers.add(observer);
    }

    @Override
    public Optional<PathListItem> getSelectedItem() {
        return Optional.ofNullable(selectedItem);
    }

    public void setSelectedItem(PathListItem selectedItem) {
        System.out.println("set selected item to " + selectedItem);
        this.selectedItem = selectedItem;
        if (selectedItem == null) {
            selectionObserver.forEach(SelectionObserver::clear);
        } else {
            selectionObserver.forEach(o->o.update(selectedItem));
        }
    }

    @Override
    public void registerSelectionObserver(SelectionObserver observer) {
        selectionObserver.add(observer);
    }

    @Override
    public void forgetMonitoredValue(String topic) {
        monitored.remove(topic);
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
                .filter(topic -> searchFilter.test(data.get(topic)))
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
                output.add(new PathListItem(key, Optional.of(data.get(pathWithSeparator + key).message()), value - 1));
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
        //addToMonitoredValues(getSearchPath(), getSelectedItem());
    }

    @Override
    public MqttConnector connector() {
        return mqttConnector;
    }

    @Override
    public void forgetMonitoredValues() {
        monitored.clear();
    }

    @Override
    public boolean monitorTopic(String topic) {
        if (data.containsKey(topic)) {
            var historicValues = new LinkedList<Message>();
            historicValues.add(data.get(topic));
            monitored.put(topic, historicValues);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isMonitored(String topic) {
        return monitored.containsKey(topic);
    }

    @Override
    public List<Message> getMonitoredValues() {
        return monitored.keySet().stream().map(data::get).collect(Collectors.toList());
    }

    @Override
    public List<Message> getHistoricValues() {
        ArrayList<Message> output = new ArrayList<>();
        monitored.values().stream().flatMap(Collection::stream).forEach(output::add);
        output.sort(Comparator.comparing(Message::time));
        return output;
    }

}
