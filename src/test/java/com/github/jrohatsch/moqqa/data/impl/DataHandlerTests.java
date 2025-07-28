package com.github.jrohatsch.moqqa.data.impl;

import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.domain.Message;
import com.github.jrohatsch.moqqa.domain.PathListItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class DataHandlerTests {
    private Datahandler datahandler;
    private TestMqttConnector mqttConnector;

    @BeforeEach
    public void init() {
        mqttConnector = new TestMqttConnector();
        datahandler = new DatahandlerImpl(mqttConnector);
    }

    public void wait(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test_that_first_level_values_are_in_path() {
        mqttConnector.mockMessage("lvl1", "value");
        wait(Duration.ofMillis(10));
        var paths = datahandler.getPathItems("");
        var expected = new PathListItem("lvl1", Optional.of("value"), 0);
        assertTrue(paths.contains(expected));
    }

    @Test
    public void test_that_second_level_values_show_parent() {
        mqttConnector.mockMessage("parent/childA", "value");
        mqttConnector.mockMessage("parent/childB", "value");
        wait(Duration.ofMillis(10));
        var paths = datahandler.getPathItems("");
        var expected = new PathListItem("parent", Optional.empty(), 2);
        assertTrue(paths.contains(expected));
    }

    @Test
    public void test_that_topic_has_child_and_value() {
        mqttConnector.mockMessage("parent", "value");
        mqttConnector.mockMessage("parent/child", "value");
        wait(Duration.ofMillis(10));

        var paths = datahandler.getPathItems("");
        var expected = new PathListItem("parent", Optional.of("value"), 1);
        assertTrue(paths.contains(expected));
    }

    @Test
    public void test_that_adding_monitored_values_works() {
        mqttConnector.mockMessage("level1", "value");
        mqttConnector.mockMessage("level1/A", "value");
        mqttConnector.mockMessage("level1/B", "value");
        wait(Duration.ofMillis(10));
        assertTrue(datahandler.addToMonitoredValues("", "level1"));
        assertTrue(datahandler.addToMonitoredValues("level1", "A"));
        assertTrue(datahandler.addToMonitoredValues("level1/", "B"));
    }

    @Test
    public void test_that_monitored_values_show_all_historic_values() {
        mqttConnector.mockMessage("topic1", "A");
        wait(Duration.ofMillis(10));
        mqttConnector.mockMessage("topic2", "B");
        wait(Duration.ofMillis(10));

        // after topics have initial values begin to monitor
        assertTrue(datahandler.addToMonitoredValues("", "topic1"));
        assertTrue(datahandler.addToMonitoredValues("", "topic2"));

        wait(Duration.ofMillis(10));
        mqttConnector.mockMessage("topic1", "C");
        wait(Duration.ofMillis(10));
        mqttConnector.mockMessage("topic2", "D");
        wait(Duration.ofMillis(10));

        var historicValues = datahandler.getHistoricValues().stream().map(Message::message).collect(Collectors.toList());

        // messages should be ordered by time
        assertEquals(List.of("A", "B", "C", "D"), historicValues);
    }

    @Test
    public void test_that_values_are_deleted() {
        mqttConnector.mockMessage("topic", "A");
        wait(Duration.ofMillis(10));
        var paths = datahandler.getPathItems("");
        assertTrue(paths.contains(new PathListItem("topic", Optional.of("A"), 0)));
        mqttConnector.mockMessage("topic", "");
        wait(Duration.ofMillis(10));
        assertTrue(datahandler.getPathItems("").isEmpty());
    }

    @Test
    public void test_that_base_values_are_filtered() {
        mqttConnector.mockMessage("ignored_filter_me_ignored", "A");
        mqttConnector.mockMessage("ignored", "A");
        wait(Duration.ofMillis(10));
        datahandler.setSearchFilter(message -> message.topic().contains("filter_me"));
        var paths = datahandler.getPathItems("");
        assertTrue(paths.contains(new PathListItem("ignored_filter_me_ignored", Optional.of("A"), 0)));
        assertEquals(1, paths.size());
    }

    @Test
    public void test_that_child_values_are_filtered() {
        mqttConnector.mockMessage("root/ignored_filter_me_ignored", "A");
        mqttConnector.mockMessage("root/ignored", "A");
        wait(Duration.ofMillis(10));
        datahandler.setSearchFilter(message -> message.topic().contains("filter_me"));
        var paths = datahandler.getPathItems("");
        assertTrue(paths.contains(new PathListItem("root", Optional.empty(), 1)));
    }
}
