package com.github.jrohatsch.moqqa.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.github.jrohatsch.moqqa.components.HistoryUpdater;
import com.github.jrohatsch.moqqa.components.MonitoredValuesUpdater;
import com.github.jrohatsch.moqqa.components.PathItemUpdater;
import com.github.jrohatsch.moqqa.components.SearchPathUpdater;
import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.domain.PathListItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UserInterface {
    private final Datahandler dataHandler;
    private JFrame frame;
    private JList<PathListItem> pathItems;
    private PathItemUpdater pathItemUpdater;

    private SearchPathUpdater searchPathUpdater;
    private MonitoredValuesUpdater monitoredValuesUpdater;
    private HistoryUpdater historyUpdater;

    public UserInterface(Datahandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    public void setup() {
        FlatLightLaf.setup();
        ToolTipManager.sharedInstance().setInitialDelay(1500);
        frame = new JFrame("Moqqa");


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
            if (selectedItem != null) {
                dataHandler.setSelectedItem(selectedItem.topic());
            }
        });

        JTabbedPane tabbedPane = new JTabbedPane();

        monitoredValuesUpdater = new MonitoredValuesUpdater(dataHandler);
        JPanel monitorFrame = monitoredValuesUpdater.init();

        historyUpdater = new HistoryUpdater(dataHandler);

        tabbedPane.add("Monitor", monitorFrame);
        tabbedPane.add("History", historyUpdater.init());
        tabbedPane.add("Publish", new JPanel());

        var splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pathItemsPane, tabbedPane);

        splitPane.setResizeWeight(1);
        frame.add(splitPane, BorderLayout.CENTER);

        frame.setFocusable(false);


        // search Path
        searchPathUpdater = new SearchPathUpdater(dataHandler);
        var toolbar = searchPathUpdater.getToolbar();
        var searchPathScroll = new JScrollPane();
        searchPathScroll.setViewportView(toolbar);


        frame.add(searchPathScroll, BorderLayout.NORTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(500,500);
    }

    public void show() {
        JFrame welcomeFrame = createWelcomeFrame();

        welcomeFrame.setVisible(true);
    }

    private JFrame createWelcomeFrame() {
        var connectFrame = new JFrame();

        connectFrame.setTitle("Connect Options");
        connectFrame.setLayout(new GridBagLayout());
        connectFrame.setSize(400,250);

        var gc = new GridBagConstraints();
        gc.insets = new Insets(5,5,5,5);
        gc.gridx = 0;
        connectFrame.add(new JLabel("Mqtt Adress:"), gc);
        gc.gridx = 1;
        var address = new JTextField("localhost:1883",15);
        connectFrame.add(address, gc);

        var connectButton = new JButton("Connect");

        connectButton.addActionListener(action -> {
            dataHandler.connect(address.getText());
            pathItemUpdater.start();
            connectFrame.setVisible(false);
            frame.setTitle(address.getText());
            frame.setVisible(true);
        });

        gc.gridx = 2;
        connectFrame.add(connectButton, gc);
        return connectFrame;
    }

    public void clear() {
        pathItemUpdater.reset();
        searchPathUpdater.clear();
        monitoredValuesUpdater.clear();
    }


    public void start() {
        pathItemUpdater.execute();
        monitoredValuesUpdater.execute();
        historyUpdater.execute();
    }
}
