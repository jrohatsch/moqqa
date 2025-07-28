package com.github.jrohatsch.moqqa.components;

import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.domain.Message;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Predicate;

public class FilterPathItems {
    private final Datahandler datahandler;
    private JTextField topicTextField;
    private JTextField valueTextField;

    public FilterPathItems(Datahandler datahandler) {
        this.datahandler = datahandler;
    }

    public JPanel init() {
        JPanel frame = new JPanel();
        frame.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
        var mainLayout = new BoxLayout(frame, BoxLayout.Y_AXIS);
        frame.setLayout(mainLayout);

        topicTextField = addTextField(frame, "Topic Filter:");
        valueTextField = addTextField(frame, "Value Filter:");


        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton removeFilter = new JButton("Reset");

        removeFilter.addActionListener(action -> {
            topicTextField.setText("");
            valueTextField.setText("");
        });


        buttonBar.add(removeFilter);
        frame.add(buttonBar);

        Predicate<Message> topicFilter = message -> message.topic().contains(topicTextField.getText());
        Predicate<Message> valueFilter = message -> message.message().contains(valueTextField.getText());

        datahandler.setSearchFilter(topicFilter.and(valueFilter));

        return frame;
    }

    private JTextField addTextField(JPanel frame, String description) {
        JPanel topicFrame = new JPanel();
        topicFrame.setLayout(new BoxLayout(topicFrame, BoxLayout.X_AXIS));
        topicFrame.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        JLabel label = new JLabel(description + "    ");
        topicFrame.add(label);

        var topicFilter = new JTextField();
        topicFilter.setMaximumSize(new Dimension(Integer.MAX_VALUE, topicFilter.getPreferredSize().height));
        topicFrame.add(topicFilter);
        frame.add(topicFrame);
        return topicFilter;
    }
}
