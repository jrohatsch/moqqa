package com.github.jrohatsch.moqqa.ui;

import com.github.jrohatsch.moqqa.utils.TextUtils;

import javax.swing.*;
import java.awt.*;

public class SettingsButton extends JMenu {

    public SettingsButton(JComponent component) {
        super("\u26ed");

        var increaseSize = new JMenuItem(TextUtils.getText("label.fontSizePlus"));
        increaseSize.addActionListener(a -> {
            sizeFontRecursive(component, 5);
        });
        add(increaseSize);
        var decreaseSize = new JMenuItem(TextUtils.getText("label.fontSizeMinus"));
        decreaseSize.addActionListener(a -> {
            sizeFontRecursive(component, -5);
        });
        add(decreaseSize);
    }

    private void sizeFontRecursive(JComponent component, float delta) {
        Font font = component.getFont();
        int style = font.getStyle();
        int size = font.getSize();

        component.setFont(font.deriveFont(style, size + delta));

        for (Component child : component.getComponents()) {
            try {
                sizeFontRecursive((JComponent) child, delta);
            } catch (ClassCastException ignored) {

            }
        }
    }

}
