package com.github.jrohatsch.moqqa.ui;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;

public class ToolTipPopup {
    public ToolTipPopup(JComponent parent, String text, Duration visible){
        var toolTip = parent.createToolTip();

        toolTip.setTipText(text);

        PopupFactory popupFactory = PopupFactory.getSharedInstance();
        Point p = parent.getLocationOnScreen();
        Popup popup = popupFactory.getPopup(parent, toolTip, p.x, p.y + 28);

        popup.show();
        Timer timer = new Timer((int) visible.toMillis(), e -> popup.hide());
        timer.setRepeats(false);
        timer.start();
    }
}
