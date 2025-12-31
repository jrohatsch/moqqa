package com.github.jrohatsch.moqqa.ui;

import com.github.jrohatsch.moqqa.data.MqttConnector;
import com.github.jrohatsch.moqqa.utils.ColorUtils;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConnectionState extends JLabel {
    private final MqttConnector connector;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public ConnectionState(MqttConnector connector) {
        this.connector = connector;
    }

    public void start() {
        executor.scheduleAtFixedRate(() -> {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    boolean isConnected = connector.isConnected();
                    setText(isConnected ? " Connected" : " Disconnected!");
                    setForeground(isConnected ? ColorUtils.BLUE : ColorUtils.RED);
                });
            } catch (InterruptedException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

}
