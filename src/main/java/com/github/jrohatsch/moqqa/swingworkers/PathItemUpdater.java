package com.github.jrohatsch.moqqa.swingworkers;

import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.domain.PathListItem;

import javax.swing.*;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

public class PathItemUpdater extends SwingWorker<DefaultListModel<PathListItem>, DefaultListModel<PathListItem>> {
    private final Datahandler dataHandler;
    private final AtomicBoolean doUpdate;
    private DefaultListModel<PathListItem> pathItemsModel;
    private String currentPath = "";

    public PathItemUpdater(Datahandler dataHandler) {
        this.dataHandler = dataHandler;
        this.doUpdate = new AtomicBoolean(false);
    }

    public JList<PathListItem> init() {
        this.pathItemsModel = new DefaultListModel<>();
        return new JList<>(pathItemsModel);
    }

    public void updatePath(String path) {
        stop();
        this.currentPath = path;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        clear();
    }

    private void updatePathItems(Collection<PathListItem> updatedPaths) {
        for (PathListItem updatedPath : updatedPaths) {
            int size = pathItemsModel.getSize();
            boolean updatedPathFoundInCurrentPaths = false;
            for (int i = 0; i < size; ++i) {
                PathListItem eachPath = pathItemsModel.get(i);

                if (eachPath.topic().equals(updatedPath.topic())) {
                    updatedPathFoundInCurrentPaths = true;
                    if (!eachPath.equals(updatedPath)) {
                        // update value
                        pathItemsModel.setElementAt(updatedPath, i);
                    }
                }
            }
            if (!updatedPathFoundInCurrentPaths) {
                pathItemsModel.addElement(updatedPath);
            }
        }
    }

    @Override
    protected DefaultListModel<PathListItem> doInBackground() throws Exception {
        while (true) {
            if (!doUpdate.get()) {
                continue;
            }
            Thread.sleep(100);
            Collection<PathListItem> updated = dataHandler.getPathItems(this.currentPath);
            updatePathItems(updated);
        }
    }

    public void clear() {
        stop();
        if (pathItemsModel != null) {
            pathItemsModel.removeAllElements();
        }
        start();
    }

    private void start() {
        doUpdate.set(true);
    }

    public void stop() {
        doUpdate.set(false);
    }
}
