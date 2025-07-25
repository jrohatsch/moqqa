package com.github.jrohatsch.moqqa.components;

import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.domain.Message;
import com.github.jrohatsch.moqqa.ui.Colors;
import com.github.jrohatsch.moqqa.utils.TextUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MonitoredValuesUpdater extends SwingWorker<Void, Void> {
    private final Datahandler datahandler;
    private final DefaultTableModel tableModel;
    private final JTable table;


    public MonitoredValuesUpdater(Datahandler datahandler) {
        this.datahandler = datahandler;
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(this.tableModel);
    }

    public JPanel init() {
        table.setSelectionBackground(Colors.SUCCESS);

        tableModel.addColumn("topic");
        tableModel.addColumn("value");

        JPanel monitorFrame = new JPanel(new BorderLayout(5, 5));
        JScrollPane monitoredItemsPane = new JScrollPane();
        monitoredItemsPane.setViewportView(table);

        var buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addButton = new JButton(TextUtils.getText("button.add"));
        addButton.addActionListener(action -> datahandler.monitorSelection());

        buttonBar.add(addButton);

        JButton removeButton = new JButton(TextUtils.getText("button.remove"));
        removeButton.setEnabled(false);
        removeButton.addActionListener(action -> {
            int selectedRowIndex = table.getSelectedRow();
            if (selectedRowIndex != -1) {
                String topic = (String) tableModel.getValueAt(selectedRowIndex, 0);
                datahandler.forgetMonitoredValue(topic);
                tableModel.removeRow(selectedRowIndex);
            }
        });
        buttonBar.add(removeButton);


        table.getSelectionModel().addListSelectionListener(action -> {
            int selectedRowIndex = table.getSelectedRow();
            removeButton.setEnabled(selectedRowIndex != -1);
        });


        JButton removeAllButton = new JButton(TextUtils.getText("button.removeAll"));
        removeAllButton.addActionListener(action -> removeAll());
        buttonBar.add(removeAllButton);

        monitorFrame.add(buttonBar, BorderLayout.NORTH);

        monitorFrame.add(monitoredItemsPane, BorderLayout.CENTER);
        return monitorFrame;
    }

    private void removeAll() {
        datahandler.forgetMonitoredValues();
        final int size = tableModel.getRowCount();
        for (int i = 0; i < size; ++i) {
            tableModel.removeRow(0);
        }
    }

    @Override
    protected Void doInBackground() throws Exception {
        while (true) {
            var values = datahandler.getMonitoredValues();
            updateMonitoredItems(values);
            Thread.sleep(100);
        }
    }

    private void updateMonitoredItems(List<Message> monitoredValues) {
        for (Message message : monitoredValues) {
            int rows = tableModel.getRowCount();
            int rowIndex = -1;
            for (int i = 0; i < rows; ++i) {
                String row = (String) tableModel.getValueAt(i, 0);
                if (row.equals(message.topic())) {
                    rowIndex = i;
                }
            }
            if (rowIndex == -1) {
                tableModel.addRow(new String[]{message.topic(), message.message()});
            } else {
                // check if value changed
                if (!tableModel.getValueAt(rowIndex, 1).equals(message.message())) {
                    tableModel.setValueAt(message.message(), rowIndex, 1);
                }
            }
        }
    }

    public void clear() {
        removeAll();
    }
}
