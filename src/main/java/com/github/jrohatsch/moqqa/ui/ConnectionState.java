package com.github.jrohatsch.moqqa.ui;

import com.github.jrohatsch.moqqa.data.MqttConnector;
import com.github.jrohatsch.moqqa.utils.IconUtils;
import com.github.jrohatsch.moqqa.utils.TextUtils;

import javax.swing.*;
import java.awt.*;
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

                    setIcon(IconUtils.get(isConnected ? "wifi-solid-full-white.svg" : "wifi-solid-full-red.svg", new Dimension(16, 16)));
                    setToolTipText(TextUtils.getText(isConnected ? "tooltip.connectionActive" : "tooltip.connectionLost"));
                });
            } catch (InterruptedException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

}
