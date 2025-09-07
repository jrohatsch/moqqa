package com.github.jrohatsch.moqqa.ui;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.ui.FlatArrowButton;
import com.github.jrohatsch.moqqa.components.*;
import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.domain.PathListItem;
import com.github.jrohatsch.moqqa.utils.TextUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

public class UserInterface {
    private final Datahandler dataHandler;
    private JFrame frame;
    private JList<PathListItem> pathItems;
    private PathItemUpdater pathItemUpdater;

    private SearchPathUpdater searchPathUpdater;
    private MonitoredValuesUpdater monitoredValuesUpdater;
    private HistoryUpdater historyUpdater;
    private PathItemInfo pathItemInfo;

    public UserInterface(Datahandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    public void setup() {
        FlatDarculaLaf.setup();
        frame = new JFrame();


        frame.setLayout(new BorderLayout());

        // split pane
        this.pathItemUpdater = new PathItemUpdater(dataHandler);
        pathItems = this.pathItemUpdater.init();
        pathItems.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList<PathListItem> list = (JList<PathListItem>) evt.getSource();
                if (evt.getClickCount() == 2) {
                    PathListItem selection = list.getSelectedValue();
                    if (selection.childTopics() > 0) {
                        searchPathUpdater.add(selection.topic());
                        list.clearSelection();
                        dataHandler.setSearchPath(searchPathUpdater.getPath());
                    }
                }
            }
        });

        dataHandler.registerPathObserver(pathItemUpdater);

        var pathItemsPane = new JScrollPane();
        pathItemsPane.setViewportView(pathItems);
        pathItems.setLayoutOrientation(JList.VERTICAL);

        pathItems.addListSelectionListener(e -> {
            var selectedItem = pathItems.getSelectedValue();
            dataHandler.setSelectedItem(selectedItem);
        });

        JTabbedPane tabbedPane = new JTabbedPane();

        historyUpdater = new HistoryUpdater(dataHandler);
        pathItemInfo = new PathItemInfo(dataHandler);

        tabbedPane.add("Info", pathItemInfo.init());
        tabbedPane.add(TextUtils.getText("label.history"), historyUpdater.init());
        tabbedPane.add(TextUtils.getText("label.publish"), new Publisher(dataHandler).init());
        tabbedPane.add("Filter", new FilterPathItems(dataHandler).init());

        var splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pathItemsPane, tabbedPane);

        splitPane.setResizeWeight(0.8);
        frame.add(splitPane, BorderLayout.CENTER);

        frame.setFocusable(false);


        // search Path
        searchPathUpdater = new SearchPathUpdater(dataHandler);
        var toolbar = searchPathUpdater.getToolbar();
        var searchPathScroll = new JScrollPane();
        searchPathScroll.setViewportView(toolbar);


        frame.add(searchPathScroll, BorderLayout.NORTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(500, 500);
    }

    public void show() throws IOException {
        JFrame welcomeFrame = createWelcomeFrame();

        welcomeFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        welcomeFrame.setVisible(true);
    }

    private JFrame createWelcomeFrame() throws IOException {
        var connectFrame = new JFrame();

        connectFrame.setTitle("Moqqa: %s".formatted(TextUtils.getText("label.connectOptions")));
        connectFrame.setLayout(new GridBagLayout());
        connectFrame.setSize(400, 250);

        var gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.gridx = 0;
        connectFrame.add(new JLabel(TextUtils.getText("label.mqttAddress")), gc);
        gc.gridx = 1;
        var address = new JTextField("localhost:1883", 15);
        connectFrame.add(address, gc);

        var connectButton = new JButton(TextUtils.getText("button.connect"));

        connectButton.addActionListener(action -> {
            // first disable connect button
            connectButton.setEnabled(false);
            boolean connected = dataHandler.connector().connect(address.getText());
            if (connected) {
                pathItemUpdater.start();
                connectFrame.setVisible(false);
                frame.setTitle("Moqqa: " + address.getText());
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.setVisible(true);
            } else {
                System.out.println("Could not connect");
            }
            // now enable again
            connectButton.setEnabled(true);
        });

        gc.gridx = 2;
        connectFrame.add(connectButton, gc);
        return connectFrame;
    }

    public void start() {
        pathItemUpdater.execute();
        historyUpdater.execute();
    }
}
