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
    private JButton connectButton;
    private JTextField mqttAddress;

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

        GridBagLayout grid = new GridBagLayout();
        GridBagConstraints gridConstraints = new GridBagConstraints();

        frame.setLayout(grid);

        gridConstraints.insets = new Insets(5, 5, 5, 5);
        gridConstraints.fill = GridBagConstraints.HORIZONTAL;

        // Row 1
        gridConstraints.gridy = 0;
        gridConstraints.gridx = 0;
        frame.add(new JLabel("Mqtt Address:"), gridConstraints);

        gridConstraints.gridy = 0;
        gridConstraints.gridx = 1;

        mqttAddress = new JTextField(40);
        mqttAddress.setText("localhost:1883");
        frame.add(mqttAddress, gridConstraints);

        gridConstraints.gridy = 0;
        gridConstraints.gridx = 2;
        connectButton = new JButton("Connect");
        connectButton.setBackground(Colors.SUCCESS);
        connectButton.setBorderPainted(false);
        connectButton.setForeground(Color.WHITE);

        frame.add(connectButton, gridConstraints);

        connectButton.addActionListener(action -> {
            if (connectButton.getBackground().equals(Colors.SUCCESS)) {
                boolean connected = dataHandler.connect(mqttAddress.getText());
                if (connected) {
                    connectButton.setBackground(Colors.DANGER);
                    mqttAddress.setEnabled(false);
                    pathItemUpdater.start();
                }
            } else if (connectButton.getBackground().equals(Colors.DANGER)) {
                dataHandler.disconnect();
                mqttAddress.setEnabled(true);
                clear();
                connectButton.setBackground(Colors.SUCCESS);
            }

        });


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

        var splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pathItemsPane, tabbedPane);

        // path items and monitored values
        gridConstraints.gridy = 3;
        gridConstraints.gridx = 0;
        gridConstraints.gridwidth = 3;
        gridConstraints.gridheight = 3;
        frame.add(splitPane, gridConstraints);


        frame.setFocusable(false);
        frame.setSize(700, 760);
        frame.setMinimumSize(new Dimension(700, 760));

        // test toolbar
        searchPathUpdater = new SearchPathUpdater(dataHandler);

        var toolbar = searchPathUpdater.getToolbar();

        gridConstraints.gridy = 1;
        gridConstraints.gridx = 0;
        gridConstraints.gridwidth = 3;
        gridConstraints.gridheight = 1;
        var searchPathScroll = new JScrollPane();
        searchPathScroll.setViewportView(toolbar);

        frame.add(searchPathScroll, gridConstraints);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void show() {
        frame.setVisible(true);
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
