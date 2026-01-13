package com.github.jrohatsch.moqqa.components;

import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.utils.ColorUtils;
import com.github.jrohatsch.moqqa.utils.TextUtils;
import com.github.jrohatsch.moqqa.utils.TimeUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class AnalyzePage {
    private final JTable values;
    private final DefaultTableModel valuesModel;
    private final Datahandler datahandler;
    private final TimeUtils timeUtils;
    private final Logger LOGGER = Logger.getLogger(getClass().getSimpleName());
    private JPanel monitoredIDs;
    private JPanel frame;
    private ScheduledExecutorService executor;


    public AnalyzePage(Datahandler datahandler) {
        this.datahandler = datahandler;
        this.valuesModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.values = new JTable(valuesModel);
        this.timeUtils = new TimeUtils();
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    public JPanel init() {
        this.valuesModel.addColumn("topic");
        this.valuesModel.addColumn("value");
        this.valuesModel.addColumn("time");

        var scroll = new JScrollPane();
        scroll.setViewportView(this.values);

        frame = new JPanel(new BorderLayout(5, 5));
        monitoredIDs = new JPanel();
        monitoredIDs.setLayout(new BoxLayout(monitoredIDs, BoxLayout.Y_AXIS));

        var topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        topBar.add(new JLabel(TextUtils.getText("label.timeZone")));

        JComboBox<String> timeZoneDropdown = new JComboBox<>(timeUtils.getZoneIds());
        topBar.add(timeZoneDropdown);
        timeZoneDropdown.setSelectedItem(timeUtils.getZoneId());
        timeZoneDropdown.addActionListener(a -> {
            stop();
            String selectedItem = (String) timeZoneDropdown.getSelectedItem();
            timeUtils.select(selectedItem);
            removeAll();
            start();
        });

        topBar.add(createExportButton());

        frame.add(topBar, BorderLayout.NORTH);
        frame.add(monitoredIDs, BorderLayout.WEST);
        frame.add(scroll, BorderLayout.CENTER);

        return frame;
    }

    private JButton createExportButton() {
        var button = new JButton(TextUtils.getText("button.export"));
        button.setBackground(ColorUtils.BLUE);

        button.addActionListener(action -> {
            stop();
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
                            System.err.println("failed to write file");
                        }
                    });
                    fileWriter.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            start();
        });

        return button;
    }

    private void removeAll() {
        final int size = valuesModel.getRowCount();
        for (int i = 0; i < size; ++i) {
            valuesModel.removeRow(0);
        }
    }

    protected void update() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                // add all monitored values
                datahandler.getHistoricValues().forEach(message -> {
                    if (valuesModel.getRowCount() == 0) {
                        valuesModel.addRow(new String[]{message.topic(), message.message(), timeUtils.format(message.time())});
                    } else {
                        // only insert if the timestamp is bigger than the timestamp on row 0!
                        String timeString = (String) valuesModel.getValueAt(0, 2);

                        if (timeUtils.compare(timeString, message.time()) < 0) {
                            valuesModel.insertRow(0, new String[]{message.topic(), message.message(), timeUtils.format(message.time())});
                        }
                    }
                });

                // remove all rows that have a topic, which is not monitored anymore
                for (int rowIndex = 0; rowIndex < valuesModel.getRowCount(); ++rowIndex) {
                    if (!datahandler.isMonitored((String) valuesModel.getValueAt(rowIndex, 0))) {
                        valuesModel.removeRow(rowIndex);
                        rowIndex--;
                    }
                }

                monitoredIDs.updateUI();
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warning("update thread interrupted");
        } catch (InvocationTargetException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public void start() {
        stop();
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(this::update, 1, 1, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
            try {
                executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                System.err.println("could not stop update thread in time");
            }
        }
    }
}
