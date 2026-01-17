package com.github.jrohatsch.moqqa.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.function.Supplier;

public class CopyButton extends JButton {

    public CopyButton(Supplier<String> toCopy) {
        setText("\u2750");
        addActionListener(a -> {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(toCopy.get()), null);
        });
    }

}
