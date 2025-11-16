package com.github.jrohatsch.moqqa.components;

import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.data.PathObserver;
import com.github.jrohatsch.moqqa.data.SelectionObserver;
import com.github.jrohatsch.moqqa.domain.PathListItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class PathItemInfo implements SelectionObserver, PathObserver  {
    private final Datahandler datahandler;
    private final JLabel fullTopicText = new JLabel("Full Topic:");
    private final JLabel fullTopic = new JLabel();
    private final JButton topicCopyButton = new JButton("Copy Topic");
    private final JLabel children = new JLabel();
    private final JLabel placeHolder = new JLabel("Select an item to inspect!");
    private String path = "";
    private JComponent topicSeparator;
    private JLabel analyzeText;
    private JButton trackValueButton;

    public PathItemInfo(Datahandler dataHandler) {
        this.datahandler = dataHandler;
    }

    public Component init() {
        datahandler.registerSelectionObserver(this);
        datahandler.registerPathObserver(this);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
        var mainLayout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(mainLayout);

        panel.add(placeHolder);

        panel.add(fullTopicText);
        fullTopicText.setFont(fullTopicText.getFont().deriveFont(Font.BOLD));
        panel.add(fullTopic);

        topicCopyButton.addActionListener(l -> {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(fullTopic.getText()), null);
        });

        panel.add(topicCopyButton);
        topicSeparator = buildSeparator();
        panel.add(topicSeparator);
        panel.add(children);


        analyzeText = new JLabel("Analyze:");
        analyzeText.setFont(fullTopicText.getFont().deriveFont(Font.BOLD));

        panel.add(analyzeText);

        trackValueButton = new JButton("Track value");
        trackValueButton.addActionListener(l -> {
            String topic = fullTopic.getText();
            if (datahandler.isMonitored(topic)) {
                datahandler.forgetMonitoredValue(topic);
            } else {
                datahandler.monitorTopic(topic);
            }
            show();
        });

        panel.add(trackValueButton);

        clear();

        return panel;
    }

    private JComponent buildSeparator() {
        JComponent separator = new JLabel("");
        separator.setBorder(new EmptyBorder(new Insets(10, 0, 10, 0)));
        return separator;
    }

    @Override
    public void update(PathListItem selection) {
        fullTopic.setText(path + selection.topic());
        children.setText("%d child topics".formatted(selection.childTopics()));
        show();
    }

    @Override
    public void clear() {
        placeHolder.setVisible(true);
        fullTopicText.setVisible(false);
        fullTopic.setVisible(false);
        topicCopyButton.setVisible(false);
        children.setVisible(false);
        topicSeparator.setVisible(false);
        analyzeText.setVisible(false);
        trackValueButton.setVisible(false);
    }

    public void show() {
        placeHolder.setVisible(false);
        fullTopicText.setVisible(true);
        fullTopic.setVisible(true);
        topicCopyButton.setVisible(true);
        topicSeparator.setVisible(true);

        datahandler.getSelectedItem().ifPresent(item -> {
            item.value().ifPresent(v -> {
                analyzeText.setVisible(true);
                if (datahandler.isMonitored(fullTopic.getText())) {
                    trackValueButton.setText("Untrack value");
                    trackValueButton.setVisible(true);
                } else {
                    trackValueButton.setText("Track value");
                    trackValueButton.setVisible(true);
                }
            });

            if (item.childTopics() > 0) {
                children.setVisible(true);
            }
        });

    }

    @Override
    public void updatePath(String path) {
        this.path = Objects.requireNonNullElse(path, "");
        show();
    }
}
