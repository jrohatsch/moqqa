package com.github.jrohatsch.moqqa.components;

import com.formdev.flatlaf.FlatLaf;
import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.data.PathObserver;
import com.github.jrohatsch.moqqa.data.SelectionObserver;
import com.github.jrohatsch.moqqa.domain.PathListItem;
import com.github.jrohatsch.moqqa.ui.CopyButton;
import com.github.jrohatsch.moqqa.ui.DeleteButton;
import com.github.jrohatsch.moqqa.utils.TextUtils;
import org.json.JSONObject;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Iterator;
import java.util.Objects;


public class PathItemInfo implements SelectionObserver, PathObserver {
    private final Datahandler datahandler;
    private final JLabel fullTopicText = new JLabel("Topic:");
    private final JLabel fullTopic = new JLabel();
    private final JLabel children = new JLabel();
    private final JLabel placeHolder = new JLabel(TextUtils.getText("label.pathitemInfoPlaceholder"));
    private CopyButton topicCopyButton;
    private String path = "";
    private JComponent topicSeparator;
    private JButton trackValueButton;
    private JTextArea topicTree;
    private JLabel topicTreeTitle;
    private CopyButton topicTreeCopyButton;
    private JScrollPane topicTreeScrollPane;
    private DeleteButton deleteButton;
    private JComponent toolTipParent;
    private JTextArea valueField;
    private JLabel valueTitle;
    private JScrollPane valueFieldScrollPane;
    private JButton valueCopyButton;
    private JToolTip retained;

    public PathItemInfo(Datahandler dataHandler, JComponent toolTipParent) {
        this.datahandler = dataHandler;
        this.toolTipParent = toolTipParent;
    }

    public PathItemInfo(Datahandler dataHandler) {
        this.datahandler = dataHandler;
    }

    public static void generateTree(JSONObject obj, String prefix, StringBuilder builder) {
        Iterator<String> keys = obj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject value = obj.getJSONObject(key);
            boolean nextIsTail = !keys.hasNext();

            builder.append(prefix)
                    .append(nextIsTail ? "\\-- " : "|-- ")
                    .append(key)
                    .append("\n");
            generateTree(value, prefix + (nextIsTail ? "    " : "|   "), builder);
        }
    }

    private JLabel createTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public Component init() {
        datahandler.registerSelectionObserver(this);
        datahandler.registerPathObserver(this);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(new Insets(10, 0, 10, 0)));
        var mainLayout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(mainLayout);

        placeHolder.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(placeHolder);

        fullTopicText.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(fullTopicText);
        fullTopicText.setFont(fullTopicText.getFont().deriveFont(Font.BOLD));

        var bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
        bar.add(fullTopic);

        bar.add(buildHorizontalSeparator());

        topicCopyButton = new CopyButton(fullTopic::getText, TextUtils.getText("tooltip.copyTopic"));

        bar.add(topicCopyButton);

        deleteButton = new DeleteButton(datahandler, fullTopic::getText, toolTipParent);

        bar.add(buildHorizontalSeparator());

        bar.add(deleteButton);

        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(bar);
        topicSeparator = buildVerticalSeparator();
        topicSeparator.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(topicSeparator);

        children.setAlignmentX(Component.LEFT_ALIGNMENT);
        children.setBorder(new EmptyBorder(new Insets(0, 0, 10, 0)));
        panel.add(children);


        valueTitle = createTitle("Value:");

        var valueTitleBar = new JPanel();
        valueTitleBar.setLayout(new BoxLayout(valueTitleBar, BoxLayout.X_AXIS));
        valueTitleBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueTitleBar.add(valueTitle);
        valueTitleBar.add(buildHorizontalSeparator());
        valueCopyButton = new CopyButton(() -> valueField.getText(), TextUtils.getText("tooltip.copyValue"));
        valueTitleBar.add(valueCopyButton);

        panel.add(valueTitleBar);

        valueField = new JTextArea();
        valueField.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueField.setBorder(new EmptyBorder(10, 10, 10, 10));

        valueFieldScrollPane = new JScrollPane(valueField);
        valueFieldScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(valueFieldScrollPane);

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

        valueTitleBar.add(buildHorizontalSeparator());
        valueTitleBar.add(trackValueButton);

        retained = valueTitleBar.createToolTip();
        retained.setTipText("Retained");
        // set the tooltip background color to the default selected color of the current look and feel
        Color background = Color.decode(FlatLaf.getGlobalExtraDefaults().get("@selectionBackground"));
        retained.setBackground(background);
        Color foreground = Color.decode(FlatLaf.getGlobalExtraDefaults().get("@selectionForeground"));
        retained.setForeground(foreground);


        valueTitleBar.add(buildHorizontalSeparator());

        valueTitleBar.add(retained);

        topicTreeTitle = createTitle("Topic tree:");
        topicTree = new JTextArea();
        topicTree.setAlignmentX(Component.LEFT_ALIGNMENT);


        topicTreeScrollPane = new JScrollPane(topicTree);
        topicTreeScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        var topicTreeBar = new JPanel();
        topicTreeBar.setLayout(new BoxLayout(topicTreeBar, BoxLayout.X_AXIS));
        topicTreeBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        topicTreeBar.add(topicTreeTitle);
        topicTreeBar.add(buildHorizontalSeparator());
        topicTreeCopyButton = new CopyButton(topicTree::getText, TextUtils.getText("tooltip.copyTopicTree"));
        topicTreeBar.add(topicTreeCopyButton);

        panel.add(buildVerticalSeparator());
        panel.add(topicTreeBar);
        panel.add(buildVerticalSeparator());
        panel.add(topicTreeScrollPane);

        clear();

        return panel;
    }

    private JComponent buildVerticalSeparator() {
        JComponent separator = new JLabel("");
        separator.setBorder(new EmptyBorder(new Insets(10, 0, 10, 0)));
        return separator;
    }

    private JComponent buildHorizontalSeparator() {
        JComponent separator = new JLabel("");
        separator.setBorder(new EmptyBorder(new Insets(0, 10, 0, 0)));
        return separator;
    }

    private void adjustTextAreaHeight(JTextArea textArea, JScrollPane scrollPane) {
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        // Calculate preferred size based on content
        int preferredHeight = (int) textArea.getPreferredSize().getHeight();
        if (preferredHeight == 0) {
            // If no height calculated, use line count
            int lineCount = Math.max(textArea.getLineCount(), 1);
            preferredHeight = lineCount * textArea.getFontMetrics(textArea.getFont()).getHeight() + 10;
        }

        // Set scroll pane to match text area height
        scrollPane.setPreferredSize(new Dimension(scrollPane.getWidth(), preferredHeight));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredHeight));
    }

    @Override
    public void update(PathListItem selection) {
        fullTopic.setText(path + selection.topic());
        children.setText("%d child topics".formatted(selection.childTopics()));
        show();
    }

    private void setVisibleValueComponents(boolean visible) {
        valueTitle.setVisible(visible);
        valueField.setVisible(visible);
        valueCopyButton.setVisible(visible);
        trackValueButton.setVisible(visible);
        valueFieldScrollPane.setVisible(visible);
    }

    private void setVisibleTopicTreeComponents(boolean visible) {
        topicTreeTitle.setVisible(visible);
        topicTree.setVisible(visible);
        topicTreeScrollPane.setVisible(visible);
        topicTreeCopyButton.setVisible(visible);
    }

    private void setVisibleTopicComponents(boolean visible) {
        fullTopicText.setVisible(visible);
        fullTopic.setVisible(visible);
        topicCopyButton.setVisible(visible);
        deleteButton.setVisible(visible);
        topicSeparator.setVisible(visible);
    }

    @Override
    public void clear() {
        placeHolder.setVisible(true);
        retained.setVisible(false);

        setVisibleTopicComponents(false);

        children.setVisible(false);
        setVisibleValueComponents(false);
        setVisibleTopicTreeComponents(false);
    }

    private void handleValuePresent(String value) {
        if (datahandler.isMonitored(fullTopic.getText())) {
            trackValueButton.setText("Untrack value");
        } else {
            trackValueButton.setText("Track value");
        }

        String valueText = value;

        try {
            JsonNode jsonValue = new ObjectMapper().readTree(value);
            if (jsonValue.isObject()) {
                var jsonObject = new JSONObject(value);
                valueText = jsonObject.toString(10);
            }
        } catch (Exception ignored) {

        }

        valueField.setText(valueText);
        adjustTextAreaHeight(valueField, valueFieldScrollPane);
        setVisibleValueComponents(true);
        retained.setVisible(datahandler.isRetained(fullTopic.getText()));
    }

    private void handleValueMissing() {
        setVisibleValueComponents(false);
    }

    public void show() {
        placeHolder.setVisible(false);
        retained.setVisible(false);

        datahandler.getSelectedItem().ifPresent(item -> {

            setVisibleTopicComponents(true);

            item.value().ifPresentOrElse(this::handleValuePresent, this::handleValueMissing);

            if (item.childTopics() > 0) {
                children.setVisible(true);

                topicTree.setText(getTopicTree(fullTopic.getText()));
                topicTree.setCaretPosition(0);
                adjustTextAreaHeight(topicTree, topicTreeScrollPane);
                setVisibleTopicTreeComponents(true);
            } else {
                children.setVisible(false);
                setVisibleTopicTreeComponents(false);
            }
        });

    }

    @Override
    public void updatePath(String path) {
        this.path = Objects.requireNonNullElse(path, "");
        show();
    }

    protected JSONObject getJSONObject(String fullTopic) {
        JSONObject jsonObject = new JSONObject();

        datahandler.getMessages(message -> message.topic().startsWith(fullTopic))
                .forEach(message -> {
                    JSONObject iterateJsonObject = jsonObject;
                    String[] subtopics = message.topic().split("/");

                    for (String eachSubtopic : subtopics) {
                        if (iterateJsonObject.has(eachSubtopic)) {
                            iterateJsonObject = iterateJsonObject.getJSONObject(eachSubtopic);
                        } else {
                            var newChild = new JSONObject();
                            iterateJsonObject.put(eachSubtopic, newChild);
                            iterateJsonObject = newChild;
                        }
                    }
                });

        return jsonObject;
    }

    protected String getTopicTree(String fullTopic) {
        var builder = new StringBuilder();

        generateTree(getJSONObject(fullTopic), "", builder);

        return builder.toString();
    }
}
