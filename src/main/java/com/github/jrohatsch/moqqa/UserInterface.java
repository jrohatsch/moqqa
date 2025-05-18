package com.github.jrohatsch.moqqa;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

public class UserInterface {
    private final DataHandler dataHandler;
    private JFrame frame;
    private JList<String> pathItems;
    private DefaultListModel<String> pathItemsModel;
    private JButton connectButton;
    private JTextField mqttAddress;
    private JTextField searchPath;

    public UserInterface(DataHandler dataHandler) {
        this.dataHandler = dataHandler;

    }

    public void setup() {
        FlatDarkLaf.setup();
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
                    clearPathItems();
                }
            } else if (connectButton.getText().equals("Disconnect")) {
                dataHandler.disconnect();
                mqttAddress.setEnabled(true);
                connectButton.setText("Connect");
                clearPathItems();
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

                if (index == -1) {
                    searchPath.setText("");
                } else {
                    searchPath.setText(path.substring(0, index + 1));
                }
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
        gridConstraints.gridwidth = 3;
        gridConstraints.gridheight = 3;

        pathItemsModel = new DefaultListModel<>();

        pathItems = new JList<>(pathItemsModel);
        pathItems.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList<String> list = (JList<String>) evt.getSource();
                if (evt.getClickCount() == 2) {
                    String selection = list.getSelectedValue();
                    if (!selection.contains(" = ")) {
                        list.clearSelection();
                        var index = selection.indexOf(" ");
                        if (index == -1) {
                            searchPath.setText(searchPath.getText() + selection + "/");
                        } else {
                            searchPath.setText(searchPath.getText() + selection.substring(0, index) + "/");
                        }
                    }
                }
            }
        });

        var scrollPane = new JScrollPane();
        scrollPane.setViewportView(pathItems);
        pathItems.setLayoutOrientation(JList.VERTICAL);

        frame.add(scrollPane, gridConstraints);

        frame.setFocusable(false);
        frame.pack();
        frame.setSize(800, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void show() {
        frame.setVisible(true);
    }

    public void clearPathItems() {
        pathItemsModel.clear();
    }

    public void updatePathItems(Collection<String> paths) {
        for (var path : paths) {
            if (!pathItemsModel.contains(path)) {
                pathItemsModel.add(0, path);
            }
        }
        var iterator = pathItemsModel.elements().asIterator();
        while (iterator.hasNext()) {
            var current = iterator.next();
            if (!paths.contains(current)) {
                pathItemsModel.removeElement(current);
            }
        }
    }

    public void loop() {
        while (true) {
            updatePathItems(dataHandler.getPathItems(searchPath.getText()));
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
