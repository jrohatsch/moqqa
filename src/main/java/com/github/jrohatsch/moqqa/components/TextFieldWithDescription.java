package com.github.jrohatsch.moqqa.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TextFieldWithDescription {
    private final JPanel panel;
    private final JTextField textField;

    public TextFieldWithDescription(String description) {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        JLabel label = new JLabel(description);
        label.setBorder(new EmptyBorder(0,0,0,20));
        panel.add(label);

        textField = new JTextField();
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, textField.getPreferredSize().height));
        panel.add(textField);
    }

    public JPanel get() {
        return panel;
    }

    public void setText(String text) {
        textField.setText(text);
    }

    public CharSequence getText() {
        return textField.getText();
    }
}
