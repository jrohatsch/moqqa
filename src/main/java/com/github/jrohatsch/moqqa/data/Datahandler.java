package com.github.jrohatsch.moqqa.data;

import com.github.jrohatsch.moqqa.domain.Message;
import com.github.jrohatsch.moqqa.domain.PathListItem;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface Datahandler {
    MqttConnector connector();

    void forgetMonitoredValues();

    boolean monitorTopic(String topic);

    boolean isMonitored(String topic);

    Collection<PathListItem> getPathItems(String path);

    void setSearchPath(String path);

    void setSearchFilter(Predicate<Message> filter);

    void registerPathObserver(PathObserver observer);

    void setSelectedItem(PathListItem pathListItem);

    void registerSelectionObserver(SelectionObserver observer);
    Optional<PathListItem> getSelectedItem();

    void forgetMonitoredValue(String topic);

    List<Message> getHistoricValues();

    void clear();
}
