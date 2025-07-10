package com.github.jrohatsch.moqqa.data.impl;

import com.github.jrohatsch.moqqa.data.Datahandler;
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
        mqttConnector.mockMessage("topic", "A");
        wait(Duration.ofMillis(10));
        assertTrue(datahandler.addToMonitoredValues("", "topic"));
        mqttConnector.mockMessage("topic", "B");
        wait(Duration.ofMillis(1));
        mqttConnector.mockMessage("topic", "C");
        wait(Duration.ofMillis(1));
        mqttConnector.mockMessage("topic", "D");
        wait(Duration.ofMillis(10));

        var historicValues = datahandler.getHistoricValues("topic").stream().map(message -> message.message()).collect(Collectors.toList());
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

}
