package com.github.jrohatsch.moqqa.ui;

import com.github.jrohatsch.moqqa.components.*;
import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.domain.PathListItem;
import com.github.jrohatsch.moqqa.utils.TextUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainPanel {
    private final Datahandler dataHandler;
    private JList<PathListItem> pathItems;
    private PathItemUpdater pathItemUpdater;
    private SearchPathUpdater searchPathUpdater;
    private AnalyzePage analyzePage;
    private PathItemInfo pathItemInfo;
    private JPanel panel;

    public MainPanel(Datahandler datahandler) {
        this.dataHandler = datahandler;
    }

    private void stepIntoSelection() {
        PathListItem selection = pathItems.getSelectedValue();
        if (selection.childTopics() > 0) {
            searchPathUpdater.add(selection.topic());
            pathItems.clearSelection();
            dataHandler.setSearchPath(searchPathUpdater.getPath());
        }
    }


    public JPanel get() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // split pane
        this.pathItemUpdater = new PathItemUpdater(dataHandler);
        pathItems = this.pathItemUpdater.init();
        pathItems.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    stepIntoSelection();
                }
            }
        });

        pathItems.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                System.out.printf("key pressed %d%n", keyEvent.getKeyCode());
                if (keyEvent.getKeyCode() == 39) {
                    stepIntoSelection();
                }
            }

        });

        dataHandler.registerPathObserver(pathItemUpdater);

        var pathItemsPane = new JScrollPane();
        pathItemsPane.setViewportView(pathItems);
        pathItems.setLayoutOrientation(JList.VERTICAL);

        pathItems.addListSelectionListener(e -> {
            var selectedItem = pathItems.getSelectedValue();
            dataHandler.setSelectedItem(selectedItem);
        });

        JTabbedPane tabbedPane = new JTabbedPane();

        analyzePage = new AnalyzePage(dataHandler);
        pathItemInfo = new PathItemInfo(dataHandler);

        tabbedPane.add("Info", pathItemInfo.init());
        tabbedPane.add(TextUtils.getText("label.history"), analyzePage.init());
        tabbedPane.add(TextUtils.getText("label.publish"), new Publisher(dataHandler).init());
        tabbedPane.add("Filter", new FilterPathItems(dataHandler).init());

        var splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pathItemsPane, tabbedPane);

        splitPane.setResizeWeight(0.8);
        panel.add(splitPane, BorderLayout.CENTER);

        panel.setFocusable(false);

        // search Path
        searchPathUpdater = new SearchPathUpdater(dataHandler);
        var toolbar = searchPathUpdater.getToolbar();
        var searchPathScroll = new JScrollPane();
        searchPathScroll.setViewportView(toolbar);

        panel.add(searchPathScroll, BorderLayout.NORTH);

        var bottomToolbar = new JMenuBar();
        bottomToolbar.add(new SettingsButton(panel));

        panel.add(bottomToolbar, BorderLayout.SOUTH);

        return panel;
    }

    public void start() {
        pathItemUpdater.start();
        analyzePage.execute();
    }

    public void setVisible(boolean b) {
        panel.setVisible(b);
    }

    public void stop() {
        pathItemUpdater.stop();
        analyzePage.stop();
        dataHandler.connector().disconnect();
        dataHandler.clear();
    }
}
