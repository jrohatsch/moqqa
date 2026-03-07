package com.github.jrohatsch.moqqa.ui;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLaf;
import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.utils.TextUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class UserInterface {
    private final Logger LOGGER = Logger.getLogger(getClass().getSimpleName());
    private final Datahandler dataHandler;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private JFrame frame;
    private ConnectionPanel connectionPanel;
    private MainPanel mainPanel;
    private PanelType activePanel;

    public UserInterface(Datahandler dataHandler) {
        this.dataHandler = dataHandler;
    }


    public void show() {
        // setup steps
        Map<String, String> extra = new HashMap<>();

        extra.put("@background", "#060A09");
        extra.put("@componentBackground", "#0D1614");
        extra.put("@canvas", "#060A09");

        extra.put("@accentColor", "#10B981");
        extra.put("@selectionBackground", "#132F2A");
        extra.put("@selectionForeground", "#10B981");

        extra.put("@focusColor", "#059669");

        FlatLaf.setGlobalExtraDefaults(extra);

        FlatLaf.setGlobalExtraDefaults(extra);
        FlatDarculaLaf.setup();

        GraphicsEnvironment ge =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            InputStream fontStream = getClass().getResourceAsStream("/fonts/roboto.ttf");
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            ge.registerFont(font);
            UIManager.put("defaultFont", font.deriveFont(14f));
        } catch (FontFormatException | IOException e) {
            LOGGER.warning("Could not load custom font, using default");
        }

        frame = new JFrame();

        addConnectionPanel();

        frame.setMinimumSize(new Dimension(400, 400));
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

        setIcon();
    }

    private void setIcon() {
        try {
            Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/moqqa.png"));
            String os = System.getProperty("os.name");

            if (os.toLowerCase().contains("mac")) {
                Taskbar.getTaskbar().setIconImage(icon);
            }
            frame.setIconImage(icon);
        } catch (Exception e) {
            LOGGER.warning("could not set icon");
        }
    }

    private void addConnectionPanel() {
        connectionPanel = new ConnectionPanel(dataHandler);
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
        connectionPanel.waitForConnection();
        connectionPanel.setVisible(false);
        addMainPanel();
        frame.setVisible(true);
        mainPanel.start();
    }

}
