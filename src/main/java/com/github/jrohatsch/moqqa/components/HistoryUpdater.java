package com.github.jrohatsch.moqqa.components;

import com.github.jrohatsch.moqqa.data.Datahandler;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class HistoryUpdater extends SwingWorker<Void, Void> {
    private final JTable values;
    private final DefaultTableModel valuesModel;
    private final Datahandler datahandler;
    private final TimeUtils timeUtils;
    private final ArrayList<String> monitoredTopics;
    private final AtomicBoolean update;
    private JPanel monitoredIDs;
    private Optional<String> selectedTopic;
    private JPanel frame;

    public HistoryUpdater(Datahandler datahandler) {
        this.datahandler = datahandler;
        this.valuesModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.values = new JTable(valuesModel);
        this.update = new AtomicBoolean(false);
        this.selectedTopic = Optional.empty();
        this.monitoredTopics = new ArrayList<>();
        this.timeUtils = new TimeUtils();
    }

    public JPanel init() {
        this.valuesModel.addColumn("topic");
        this.valuesModel.addColumn("time");
        this.valuesModel.addColumn("value");

        var scroll = new JScrollPane();
        scroll.setViewportView(this.values);

        frame = new JPanel(new BorderLayout(5, 5));
        monitoredIDs = new JPanel();
        monitoredIDs.setLayout(new BoxLayout(monitoredIDs, BoxLayout.Y_AXIS));

        var topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        topBar.add(new JLabel("Timezone:"));

        JComboBox timeZoneDropdown = new JComboBox<>(timeUtils.getZoneIds());
        topBar.add(timeZoneDropdown);
        timeZoneDropdown.setSelectedItem(timeUtils.getZoneId());
        timeZoneDropdown.addActionListener(a -> {
            update.set(false);
            String selectedItem = (String) timeZoneDropdown.getSelectedItem();
            timeUtils.select(selectedItem);
            removeAll();
            update.set(true);
        });

        topBar.add(createExportButton());

        frame.add(topBar, BorderLayout.NORTH);
        frame.add(monitoredIDs, BorderLayout.WEST);
        frame.add(scroll, BorderLayout.CENTER);

        return frame;
    }

    private JButton createExportButton() {
        var button = new JButton("Export");

        button.addActionListener(action -> {
            update.set(false);
            String[] linesToWrite = ExportUtils.exportTableDataAsTabSeparatedValues(valuesModel);
            var fileChooser = new JFileChooser();
            var commaFileFilter = new FileNameExtensionFilter("Tab Separated Values (.tsv)", ".tsv");
            fileChooser.setFileFilter(commaFileFilter);

            int option = fileChooser.showSaveDialog(frame);
            if (option == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.endsWith(".tsv")) {
                    filePath += ".tsv";
                }
                try {
                    var fileWriter = new FileWriter(filePath);
                    Arrays.stream(linesToWrite).forEach(line -> {
                        try {
                            fileWriter.write(line);
                        } catch (IOException e) {
                        }
                    });
                    fileWriter.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            update.set(true);
        });

        return button;
    }

    private void removeAll() {
        final int size = valuesModel.getRowCount();
        for (int i = 0; i < size; ++i) {
            valuesModel.removeRow(0);
        }
    }

    private void updateSelectedTopic(String topic) {
        update.set(false);
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {

        }
        final int size = valuesModel.getRowCount();
        for (int i = 0; i < size; ++i) {
            valuesModel.removeRow(0);
        }

        selectedTopic = Optional.of(topic);

        update.set(true);
    }

    @Override
    protected Void doInBackground() {
        // start updating
        update.set(true);

        try {
            while (true) {
                if (!update.get()) {
                    continue;
                }
                // first create a button for each monitored item
                datahandler.getMonitoredValues().forEach(message -> {
                    final String topic = message.topic();
                    if (!monitoredTopics.contains(topic)) {
                        monitoredTopics.add(topic);
                        final int ID = monitoredTopics.size();
                        var button = new JButton(String.valueOf(ID));
                        button.setToolTipText(message.topic());
                        button.addActionListener(action -> {
                            // first check if topic will be switched
                            if (selectedTopic.isEmpty() || !selectedTopic.get().equals(topic)) {
                                updateSelectedTopic(topic);
                            }
                        });
                        monitoredIDs.add(button);
                    }
                });

                // init selected topic
                if (selectedTopic.isEmpty() && monitoredTopics.size() > 0) {
                    selectedTopic = Optional.of(monitoredTopics.get(0));
                }

                if (selectedTopic.isPresent()) {
                    // add values for selected topic
                    var values = datahandler.getHistoricValues(selectedTopic.get());
                    for (var message : values) {
                        if (valuesModel.getRowCount() == 0) {
                            valuesModel.addRow(new String[]{message.topic(), timeUtils.format(message.time()), message.message()});
                        } else {
                            // only insert if the timestamp is bigger than the timestamp on row 0!
                            String timeString = (String) valuesModel.getValueAt(0, 1);

                            if (timeUtils.compare(timeString, message.time()) < 0) {
                                valuesModel.insertRow(0, new String[]{message.topic(), timeUtils.format(message.time()), message.message()});
                            }
                        }
                    }
                }

                monitoredIDs.updateUI();
                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
