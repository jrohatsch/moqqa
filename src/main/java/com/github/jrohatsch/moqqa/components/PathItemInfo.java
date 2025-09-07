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
    private JLabel fullTopicText = new JLabel("Full Topic:");
    private JLabel fullTopic = new JLabel();
    private JLabel value = new JLabel();
    private JLabel children = new JLabel();
    private JLabel placeHolder = new JLabel("Select an item to inspect!");
    private String path = "";
    private JPanel panel;

    public PathItemInfo(Datahandler dataHandler) {
        this.datahandler = dataHandler;
    }

    public Component init() {
        datahandler.registerSelectionObserver(this);
        datahandler.registerPathObserver(this);

        panel = new JPanel();
        panel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
        var mainLayout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(mainLayout);

        panel.add(placeHolder);
        panel.add(fullTopicText);
        fullTopicText.setFont(fullTopicText.getFont().deriveFont(Font.BOLD));
        panel.add(fullTopic);
        panel.add(children);

        clear();

        return panel;
    }

    @Override
    public void update(PathListItem selection) {
        fullTopic.setText(path + selection.topic());
        children.setText("%d child topics".formatted(selection.childTopics()));
        placeHolder.setVisible(false);
        fullTopic.setVisible(true);
        children.setVisible(true);
    }

    @Override
    public void clear() {
        placeHolder.setVisible(true);
        fullTopicText.setVisible(false);
        fullTopic.setVisible(false);
        children.setVisible(false);
    }

    @Override
    public void updatePath(String path) {
        this.path = Objects.requireNonNullElse(path, "");
    }
}
