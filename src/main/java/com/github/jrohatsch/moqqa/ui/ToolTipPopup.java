package com.github.jrohatsch.moqqa.ui;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;

public class ToolTipPopup {
    private final javax.swing.Popup popup;

    private ToolTipPopup(JComponent parent, String text, Duration visible, int xOffset, int yOffset) {
        var toolTip = parent.createToolTip();

        toolTip.setTipText(text);

        PopupFactory popupFactory = PopupFactory.getSharedInstance();
        Point p = parent.getLocationOnScreen();
        popup = popupFactory.getPopup(parent, toolTip, p.x + xOffset, p.y + yOffset);

        popup.show();

        if (!visible.isZero()) {
            Timer timer = new Timer((int) visible.toMillis(), e -> popup.hide());
            timer.setRepeats(false);
            timer.start();
        }

    }

    static ToolTipPopup bottomOutside(JComponent parent, String text, Duration visible) {
        return new ToolTipPopup(parent, text, visible, 0, 28);
    }

    static ToolTipPopup bottomRightCorner(JComponent parent, String text, Duration visible) {
        int xOffset = parent.getWidth() - (text.length() * 8);
        int yOffset = parent.getHeight() - 50;

        return new ToolTipPopup(parent, text, visible, xOffset, yOffset);
    }

    public void hide() {
        popup.hide();
    }
}
