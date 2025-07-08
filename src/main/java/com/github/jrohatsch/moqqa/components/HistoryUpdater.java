package com.github.jrohatsch.moqqa.components;

import com.github.jrohatsch.moqqa.data.Datahandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class HistoryUpdater extends SwingWorker<Void, Void> {
    private final JTable values;
    private final DefaultTableModel valuesModel;
    private final Datahandler datahandler;
    private JPanel monitoredIDs;
    private ArrayList<String> monitoredTopics;
    private Optional<String> selectedTopic;
    private AtomicBoolean update;

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
    }

    public JPanel init() {
        this.valuesModel.addColumn("topic");
        this.valuesModel.addColumn("time");
        this.valuesModel.addColumn("value");

        var scroll = new JScrollPane();
        scroll.setViewportView(this.values);

        JPanel frame = new JPanel(new BorderLayout(5, 5));
        monitoredIDs = new JPanel();
        monitoredIDs.setLayout(new BoxLayout(monitoredIDs, BoxLayout.Y_AXIS));
        frame.add(monitoredIDs, BorderLayout.WEST);
        frame.add(scroll, BorderLayout.CENTER);

        return frame;
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
                            valuesModel.addRow(new String[]{message.topic(), message.time().toString(), message.message()});
                        } else {
                            // only insert if the timestamp is bigger than the timestamp on row 0!
                            String timeString = (String) valuesModel.getValueAt(0, 1);
                            Instant latestTime = Instant.parse(timeString);
                            if (!message.time().equals(latestTime) && message.time().isAfter(latestTime)) {
                                valuesModel.insertRow(0, new String[]{message.topic(), message.time().toString(), message.message()});
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
