package com.github.jrohatsch.moqqa.components;

import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.utils.TextUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Publisher {
    private final Datahandler datahandler;

    public Publisher(Datahandler datahandler) {
        this.datahandler = datahandler;
    }

    public JPanel init() {
        var frame = new JPanel(new BorderLayout(5, 5));
        var topicTextField = new TextFieldWithDescription("Topic:");

        frame.add(topicTextField.get(), BorderLayout.NORTH);
        JTextArea messageTextArea = new JTextArea();
        frame.add(messageTextArea, BorderLayout.CENTER);
        var buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JCheckBox retainedCheckBox = new JCheckBox("retained");
        buttonBar.add(retainedCheckBox);
        var publishButton = new JButton("publish");
        publishButton.addActionListener(action -> datahandler.connector().publish((String) topicTextField.getText(), messageTextArea.getText(), retainedCheckBox.isSelected()));
        buttonBar.add(publishButton);
        frame.add(buttonBar, BorderLayout.SOUTH);
        return frame;
    }
}
