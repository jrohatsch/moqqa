package com.github.jrohatsch.moqqa.ui;

import com.formdev.flatlaf.FlatDarculaLaf;
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
import java.io.IOException;

public class UserInterface {
    private final Datahandler dataHandler;
    private JFrame frame;
    private JList<PathListItem> pathItems;
    private PathItemUpdater pathItemUpdater;

    private SearchPathUpdater searchPathUpdater;
    private AnalyzePage analyzePage;
    private PathItemInfo pathItemInfo;

    public UserInterface(Datahandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    private void stepIntoSelection() {
        PathListItem selection = pathItems.getSelectedValue();
        if (selection.childTopics() > 0) {
            searchPathUpdater.add(selection.topic());
            pathItems.clearSelection();
            dataHandler.setSearchPath(searchPathUpdater.getPath());
        }
    }

    public void setup() {
        FlatDarculaLaf.setup();
        frame = new JFrame();

        frame.setLayout(new BorderLayout());

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
        frame.add(splitPane, BorderLayout.CENTER);

        frame.setFocusable(false);

        // search Path
        searchPathUpdater = new SearchPathUpdater(dataHandler);
        var toolbar = searchPathUpdater.getToolbar();
        var searchPathScroll = new JScrollPane();
        searchPathScroll.setViewportView(toolbar);

        frame.add(searchPathScroll, BorderLayout.NORTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(500, 500);
    }

    public void show() throws IOException {
        JFrame welcomeFrame = createWelcomeFrame();

        welcomeFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        welcomeFrame.setVisible(true);
    }

    private JFrame createWelcomeFrame() throws IOException {
        return new ConnectionFrame(dataHandler, ()->{
                pathItemUpdater.start();
                frame.setTitle("Moqqa");
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.setVisible(true);
        }).get();
    }

    public void start() {
        pathItemUpdater.execute();
        analyzePage.execute();
    }
}
