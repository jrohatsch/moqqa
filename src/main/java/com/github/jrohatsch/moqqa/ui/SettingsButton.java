package com.github.jrohatsch.moqqa.ui;

import com.github.jrohatsch.moqqa.utils.IconUtils;
import com.github.jrohatsch.moqqa.utils.TextUtils;

import javax.swing.*;
import java.awt.*;

public class SettingsButton extends JMenu {


    public SettingsButton(JComponent component) {
        setIcon(IconUtils.get("gear-solid-full.svg", new Dimension(16, 16)));

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

        var scaleUIBar = new JPanel();
        scaleUIBar.setLayout(new BoxLayout(scaleUIBar, BoxLayout.X_AXIS));
        scaleUIBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        scaleUIBar.add(new JLabel("Scale: "));
        scaleUIBar.add(new JCheckBox("Auto", true));
        scaleUIBar.add(new JButton("100%"));
        scaleUIBar.add(new JButton("150%"));
        scaleUIBar.add(new JButton("150%"));
        scaleUIBar.add(new JButton("200%"));
        scaleUIBar.add(new JButton("250%"));

        // TODO scale accordingly
        //System.setProperty("sun.java2d.uiScale.enabled", "true");
        //System.setProperty("sun.java2d.uiScale", "2");

        add(scaleUIBar);

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
