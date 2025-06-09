package com.github.jrohatsch.moqqa.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.domain.Message;
import com.github.jrohatsch.moqqa.domain.PathListItem;
import com.github.jrohatsch.moqqa.swingworkers.PathItemUpdater;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UserInterface {
    private final Datahandler dataHandler;
    private JFrame frame;
    private JList<PathListItem> pathItems;
    private PathItemUpdater pathItemUpdater;
    private JButton connectButton;
    private JTextField mqttAddress;
    private JTextField searchPath;
    private JButton monitorButton;
    private JTable monitoredValues;
    private DefaultTableModel monitoredValuesModel;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

    public UserInterface(Datahandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    public void setup() {
        FlatLightLaf.setup();
        frame = new JFrame("Moqqa");

        GridBagLayout grid = new GridBagLayout();
        GridBagConstraints gridConstraints = new GridBagConstraints();

        frame.setLayout(grid);

        gridConstraints.insets = new Insets(5, 5, 5, 5);
        gridConstraints.fill = GridBagConstraints.HORIZONTAL;

        // Row 1: Enter Number 1
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

        frame.add(connectButton, gridConstraints);

        connectButton.addActionListener(action -> {
            if (connectButton.getText().equals("Connect")) {
                boolean connected = dataHandler.connect(mqttAddress.getText());
                if (connected) {
                    connectButton.setText("Disconnect");
                    mqttAddress.setEnabled(false);
                    clear();
                }
            } else if (connectButton.getText().equals("Disconnect")) {
                dataHandler.disconnect();
                mqttAddress.setEnabled(true);
                connectButton.setText("Connect");
                clear();
            }

        });

        // Row 2:
        gridConstraints.gridy = 1;
        gridConstraints.gridx = 0;

        var backButton = new JButton("Back");
        backButton.addActionListener(action -> {
            String path = searchPath.getText();
            // remove / if it is at the end
            if (path.endsWith("/") && path.length() > 1) {
                path = path.substring(0, path.length() - 1);
            }
            if (path.length() > 0) {
                var index = path.lastIndexOf("/");

                String newPath;
                if (index == -1) {
                    newPath = "";
                } else {
                    newPath = path.substring(0, index + 1);
                }
                pathItemUpdater.updatePath(newPath);
                searchPath.setText(newPath);
            }
        });
        frame.add(backButton, gridConstraints);

        gridConstraints.gridy = 1;
        gridConstraints.gridx = 1;
        gridConstraints.gridwidth = 2;
        searchPath = new JTextField(27);
        frame.add(searchPath, gridConstraints);

        gridConstraints.gridy = 2;
        gridConstraints.gridx = 0;
        gridConstraints.gridwidth = 1;
        monitorButton = new JButton("Monitor");
        monitorButton.setEnabled(false);


        monitorButton.addActionListener(action -> {
            String path = searchPath.getText();
            String item = pathItems.getSelectedValue().toString();
            dataHandler.addToMonitoredValues(path, item);
        });
        frame.add(monitorButton, gridConstraints);

        gridConstraints.gridy = 3;
        gridConstraints.gridx = 0;
        gridConstraints.gridwidth = 3;
        gridConstraints.gridheight = 3;


        this.pathItemUpdater = new PathItemUpdater(dataHandler);
        pathItems = this.pathItemUpdater.init();
        pathItems.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList<PathListItem> list = (JList<PathListItem>) evt.getSource();
                if (evt.getClickCount() == 2) {
                    PathListItem selection = list.getSelectedValue();
                    if (selection.value().isEmpty()) {
                        list.clearSelection();
                        String newPath = searchPath.getText() + selection.topic() + "/";
                        pathItemUpdater.updatePath(newPath);
                        searchPath.setText(newPath);
                    }
                }
            }
        });

        var scrollPane = new JScrollPane();
        scrollPane.setViewportView(pathItems);
        pathItems.setLayoutOrientation(JList.VERTICAL);

        pathItems.addListSelectionListener(e -> {
            var selectedItem = pathItems.getSelectedValue();
            if (selectedItem != null) {
                monitorButton.setEnabled(selectedItem.value().isPresent());
            } else {
                monitorButton.setEnabled(false);
            }
        });

        frame.add(scrollPane, gridConstraints);

        monitoredValuesModel = new DefaultTableModel();
        monitoredValues = new JTable(monitoredValuesModel);
        monitoredValuesModel.addColumn("topic");
        monitoredValuesModel.addColumn("value");

        JScrollPane monitoredScrollPane = new JScrollPane();
        monitoredScrollPane.setViewportView(monitoredValues);

        gridConstraints.gridy = 6;
        gridConstraints.gridx = 0;
        gridConstraints.gridwidth = 3;
        gridConstraints.gridheight = 1;
        frame.add(monitoredScrollPane, gridConstraints);


        frame.setFocusable(false);
        frame.pack();
        frame.setSize(700, 720);
        frame.setResizable(false);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void show() {
        frame.setVisible(true);
    }

    public void clear() {
        System.out.println("clear paths");
        pathItemUpdater.clear();
        int rows = monitoredValuesModel.getRowCount();
        for (int i = 0; i < rows; ++i) {
            monitoredValuesModel.removeRow(0);
        }
        dataHandler.forgetMonitoredValues();
        searchPath.setText("");
    }



    public void start() {
        pathItemUpdater.execute();

        executorService.scheduleAtFixedRate(()->{
            updateMonitoredItems(dataHandler.getMonitoredValues());
        },10,10, TimeUnit.MILLISECONDS);
    }

    private void updateMonitoredItems(List<Message> monitoredValues) {
        for (Message message : monitoredValues) {
            int rows = this.monitoredValuesModel.getRowCount();
            int rowIndex = -1;
            for (int i = 0; i < rows; ++i) {
                String row = (String) this.monitoredValuesModel.getValueAt(i, 0);
                if (row.equals(message.topic())) {
                    rowIndex = i;
                }
            }
            if (rowIndex == -1) {
                this.monitoredValuesModel.addRow(new String[]{message.topic(), message.message()});
            } else {
                // check if value changed
                if (!this.monitoredValuesModel.getValueAt(rowIndex, 1).equals(message.message())) {
                    this.monitoredValuesModel.setValueAt(message.message(), rowIndex, 1);
                }
            }
        }
    }
}
