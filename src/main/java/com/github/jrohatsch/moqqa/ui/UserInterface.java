package com.github.jrohatsch.moqqa.ui;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.utils.TextUtils;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserInterface {
    private final Datahandler dataHandler;
    private JFrame frame;
    private ConnectionPanel connectionPanel;
    private MainPanel mainPanel;
    private PanelType activePanel;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public UserInterface(Datahandler dataHandler) {
        this.dataHandler = dataHandler;
    }


    public void show() {
        FlatDarculaLaf.setup();

        frame = new JFrame();

        addConnectionPanel();

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (activePanel == PanelType.MAIN) {
                    mainPanel.setVisible(false);
                    mainPanel.stop();
                    addConnectionPanel();
                } else {
                    e.getWindow().dispose();
                    System.exit(0);
                }
            }
        });
    }

    private void addConnectionPanel() {
        connectionPanel = new ConnectionPanel(dataHandler, this::waitForConnection);
        frame.add(connectionPanel.get());
        frame.setTitle("Moqqa: %s".formatted(TextUtils.getText("label.connectOptions")));
        activePanel = PanelType.CONNECTION;
        executorService.submit(this::waitForConnection);
    }

    private void addMainPanel() {
        mainPanel = new MainPanel(dataHandler);
        frame.add(mainPanel.get());
        frame.setTitle("Moqqa: %s".formatted(dataHandler.connector().getAddress()));
        activePanel = PanelType.MAIN;
    }


    public void waitForConnection() {
        while (!connectionPanel.connected()) {
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        connectionPanel.setVisible(false);
        addMainPanel();
        frame.setVisible(true);
        mainPanel.start();
    }

    public void setFontSize(int size) {
        UIManager.put("Label.font", new FontUIResource(new Font("Default", Font.PLAIN, size)));
        UIManager.put("Button.font", new FontUIResource(new Font("Default", Font.BOLD, size)));
        UIManager.put("TextField.font", new FontUIResource(new Font("Default", Font.PLAIN, size)));
        UIManager.put("ComboBox.font", new FontUIResource(new Font("Default", Font.PLAIN, size)));
        UIManager.put("CheckBox.font", new FontUIResource(new Font("Default", Font.PLAIN, size)));
        UIManager.put("TabbedPane.font", new FontUIResource(new Font("Default", Font.PLAIN, size)));
    }

}
