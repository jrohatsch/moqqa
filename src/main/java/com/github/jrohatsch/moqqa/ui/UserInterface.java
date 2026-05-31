package com.github.jrohatsch.moqqa.ui;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.session.AppConfigHandler;
import com.github.jrohatsch.moqqa.session.impl.JsonAppConfigHandler;
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
    private final AppConfigHandler appConfigHandler = new JsonAppConfigHandler();

    public UserInterface(Datahandler dataHandler) {
        this.dataHandler = dataHandler;
    }


    public void show() {
        // setup steps - Modern dark theme with warm accents
        Map<String, String> extra = new HashMap<>();

        // Modern dark color palette
        extra.put("@background", "#1a1a1a");           // Dark background
        extra.put("@componentBackground", "#242424");  // Slightly lighter dark
        extra.put("@canvas", "#1a1a1a");               // Canvas background

        // Text and accent colors - warm accents on dark
        extra.put("@accentColor", "#c9b5a0");          // Warm beige accent
        extra.put("@selectionBackground", "#3a3a3a");  // Dark selection
        extra.put("@selectionForeground", "#e8e4dc");  // Light text
        extra.put("@focusColor", "#8b7355");           // Warm brown focus ring

        // Modern styling
        extra.put("@foreground", "#e8e4dc");           // Light text
        extra.put("@textComponentForeground", "#e8e4dc");
        extra.put("@borderColor", "#3a3a3a");          // Subtle dark border
        extra.put("@componentBorder", "#3a3a3a");
        extra.put("@buttonBackground", "#242424");
        extra.put("@buttonFocusedBackground", "#2f2f2f");
        extra.put("@buttonHoverBackground", "#2f2f2f");
        
        
        // Smooth shadows and effects
        extra.put("@Component.focusWidth", "2");       // Focus ring width
        extra.put("@Component.innerFocusWidth", "1");

        FlatLaf.setGlobalExtraDefaults(extra);

        var appConfig = appConfigHandler.loadConfig();

        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("sun.java2d.uiScale", String.valueOf(appConfig.scalingFactor()));

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
        connectionPanel = new ConnectionPanel(dataHandler, appConfigHandler);
        frame.add(connectionPanel.get());
        frame.setTitle("Moqqa: %s".formatted(TextUtils.getText("label.connectOptions")));
        activePanel = PanelType.CONNECTION;
        executorService.submit(this::waitForConnection);
    }

    private void addMainPanel() {
        mainPanel = new MainPanel(dataHandler, appConfigHandler);
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
