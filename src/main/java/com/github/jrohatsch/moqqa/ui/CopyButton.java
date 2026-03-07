package com.github.jrohatsch.moqqa.ui;

import com.github.jrohatsch.moqqa.utils.IconUtils;
import com.github.jrohatsch.moqqa.utils.TextUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.time.Duration;
import java.util.function.Supplier;

public class CopyButton extends JButton {

    public CopyButton(Supplier<String> toCopy, String toolTip) {
        setIcon(IconUtils.get("copy-solid-full.svg", new Dimension(18, 18)));
        setToolTipText(toolTip);
        addActionListener(a -> {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(toCopy.get()), null);
            ToolTipPopup.bottomOutside(this, TextUtils.getText("tooltip.afterCopy"), Duration.ofSeconds(3));
        });
    }

}
