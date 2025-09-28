package com.github.jrohatsch.moqqa.components;

import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.data.PathObserver;
import com.github.jrohatsch.moqqa.data.SelectionObserver;
import com.github.jrohatsch.moqqa.domain.PathListItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;

public class PathItemInfo implements SelectionObserver, PathObserver  {
    private final Datahandler datahandler;
    private final JLabel fullTopicText = new JLabel("Full Topic:");
    private final JLabel fullTopic = new JLabel();
    private final JLabel children = new JLabel();
    private final JLabel placeHolder = new JLabel("Select an item to inspect!");
    private String path = "";
    private JSeparator topicSeparator;
    private JSeparator childrenSeparator;
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
        topicSeparator = new JSeparator();
        panel.add(topicSeparator);
        panel.add(children);
        childrenSeparator = new JSeparator();
        panel.add(childrenSeparator);

        analyzeText = new JLabel("Analyze:");
        analyzeText.setFont(fullTopicText.getFont().deriveFont(Font.BOLD));

        panel.add(analyzeText);

        trackValueButton = new JButton("Track value");
        trackValueButton.addActionListener(l -> {
            System.out.printf("monitoring %s\n",fullTopic.getText());
            datahandler.monitorTopic(fullTopic.getText());
        });

        panel.add(trackValueButton);

        clear();

        return panel;
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
        children.setVisible(false);
        topicSeparator.setVisible(false);
        analyzeText.setVisible(false);
        trackValueButton.setVisible(false);
    }

    public void show() {
        placeHolder.setVisible(false);
        fullTopicText.setVisible(true);
        fullTopic.setVisible(true);
        children.setVisible(true);
        topicSeparator.setVisible(true);

        datahandler.getSelectedItem().ifPresent(item -> item.value().ifPresent(v -> {
                    analyzeText.setVisible(true);
                    trackValueButton.setVisible(true);
                }));

    }

    @Override
    public void updatePath(String path) {
        this.path = Objects.requireNonNullElse(path, "");
        show();
    }
}
