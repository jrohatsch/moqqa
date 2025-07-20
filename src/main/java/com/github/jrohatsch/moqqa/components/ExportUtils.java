package com.github.jrohatsch.moqqa.components;

import javax.swing.table.TableModel;

public class ExportUtils {
    public static String[] exportTableDataAsTabSeparatedValues(TableModel tableModel) {
        final int rows = tableModel.getRowCount();
        final int columns = tableModel.getColumnCount();
        String[] lines = new String[rows + 1];
        final String separator = "\t";

        // write header
        StringBuilder header = new StringBuilder();
        for (int columnIndex = 0; columnIndex < columns; ++columnIndex) {
            header.append(tableModel.getColumnName(columnIndex));
            if (columnIndex == columns - 1) {
                header.append("\n");
            } else {
                header.append(separator);
            }
        }
        lines[0] = header.toString();

        // write values
        for (int rowIndex = 0; rowIndex < rows; ++rowIndex) {
            StringBuilder line = new StringBuilder();
            for (int columnIndex = 0; columnIndex < columns; ++columnIndex) {
                line.append(tableModel.getValueAt(rowIndex, columnIndex));
                if (columnIndex == columns - 1) {
                    line.append("\n");
                } else {
                    line.append(separator);
                }
            }
            lines[rowIndex + 1] = line.toString();
        }

        return lines;
    }
}
