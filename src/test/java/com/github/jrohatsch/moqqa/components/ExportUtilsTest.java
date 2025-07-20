package com.github.jrohatsch.moqqa.components;

import org.junit.jupiter.api.Test;

import javax.swing.table.DefaultTableModel;

import java.util.List;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

class ExportUtilsTest {

    @Test
    void exportTableDataAsTabSeparatedValues() {
        var model = new DefaultTableModel();
        model.addColumn("A");
        model.addColumn("B");
        model.addColumn("C");
        model.addRow(new String[]{"valueA", "valueB", "valueC"});
        String[] tsvLines = ExportUtils.exportTableDataAsTabSeparatedValues(model);
        assertEquals(2, tsvLines.length);
        assertEquals("A\tB\tC\n", tsvLines[0]);
        assertEquals("valueA\tvalueB\tvalueC\n", tsvLines[1]);
    }
}