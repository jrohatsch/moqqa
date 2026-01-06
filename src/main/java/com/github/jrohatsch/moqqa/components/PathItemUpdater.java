package com.github.jrohatsch.moqqa.components;

import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.data.PathObserver;
import com.github.jrohatsch.moqqa.domain.PathListItem;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PathItemUpdater implements PathObserver {
    private final Datahandler dataHandler;
    private DefaultListModel<PathListItem> pathItemsModel;
    private String currentPath = "";
    private ScheduledExecutorService executor;

    public PathItemUpdater(Datahandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    public JList<PathListItem> init() {
        this.pathItemsModel = new DefaultListModel<>();
        var list = new JList<>(pathItemsModel);
        list.setCellRenderer(new PathItemRenderer());
        return list;
    }

    @Override
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

    private void updatePathItems(Collection<PathListItem> updatedPaths) throws InterruptedException, InvocationTargetException {
        for (PathListItem updatedPath : updatedPaths) {
            int size = pathItemsModel.getSize();
            boolean updatedPathFoundInCurrentPaths = false;
            for (int i = 0; i < size; ++i) {
                try {
                    PathListItem eachPath = pathItemsModel.get(i);

                    if (eachPath.topic().equals(updatedPath.topic())) {
                        updatedPathFoundInCurrentPaths = true;
                        if (!eachPath.equals(updatedPath)) {
                            // update value
                            int finalI = i;
                            SwingUtilities.invokeAndWait(() -> pathItemsModel.setElementAt(updatedPath, finalI));
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException ignored) {
                    return;
                }
            }
            if (!updatedPathFoundInCurrentPaths && pathItemsModel.getSize() < Integer.MAX_VALUE) {
                SwingUtilities.invokeAndWait(() -> pathItemsModel.addElement(updatedPath));
            }
        }
        Iterator<PathListItem> iterator = pathItemsModel.elements().asIterator();
        while (iterator.hasNext()) {
            PathListItem displayed = iterator.next();

            if (!updatedPaths.contains(displayed)) {
                SwingUtilities.invokeAndWait(() -> pathItemsModel.removeElement(displayed));
            }
        }
    }

    protected void update() {
            Collection<PathListItem> updated = dataHandler.getPathItems(this.currentPath);
        try {
            updatePathItems(updated);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("update thread interrupted");
        } catch (InvocationTargetException e) {
            System.err.println(e);
        }
    }

    public void clear() {
        stop();
        if (pathItemsModel != null) {
            pathItemsModel.removeAllElements();
        }
        start();
    }

    public void start() {
        stop();
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(this::update,1, 1, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if(executor != null) {
            executor.shutdownNow();
            try {
                executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                System.err.println("could not stop update thread in time");
            }
        }
    }
}
