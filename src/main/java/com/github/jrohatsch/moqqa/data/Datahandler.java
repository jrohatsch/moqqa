package com.github.jrohatsch.moqqa.data;

import com.github.jrohatsch.moqqa.domain.Message;
import com.github.jrohatsch.moqqa.domain.PathListItem;

import java.util.Collection;
import java.util.List;

public interface Datahandler {
    MqttConnector connector();

    void forgetMonitoredValues();

    boolean addToMonitoredValues(String path, String item);

    boolean isMonitored(String topic);

    List<Message> getMonitoredValues();

    Collection<PathListItem> getPathItems(String currentPath);

    void monitorSelection();

    void setSearchPath(String path);

    void registerPathObserver(PathObserver observer);

    void setSelectedItem(String topic);

    void forgetMonitoredValue(String topic);

    List<Message> getHistoricValues();
}
