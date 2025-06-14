package com.github.jrohatsch.moqqa.data.impl;

import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.domain.PathListItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

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
}
