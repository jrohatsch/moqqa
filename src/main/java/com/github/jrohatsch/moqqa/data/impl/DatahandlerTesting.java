package com.github.jrohatsch.moqqa.data.impl;

import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.domain.Message;
import com.github.jrohatsch.moqqa.domain.PathListItem;

import java.util.*;

public class DatahandlerTesting implements Datahandler {
    private boolean connected = false;
    @Override
    public boolean connect(String url) {
        this.connected = true;
        return true;
    }

    @Override
    public void disconnect() {
        this.connected = false;
    }

    @Override
    public void forgetMonitoredValues() {

    }

    @Override
    public void addToMonitoredValues(String path, String item) {

    }

    @Override
    public List<Message> getMonitoredValues() {
        return null;
    }

    @Override
    public Collection<PathListItem> getPathItems(String currentPath) {
        if (connected == false) {
            return List.of();
        }

        if (currentPath.equals("")) {
            return List.of(new PathListItem("root", Optional.empty(), 4));
        }

        if (currentPath.equals("root/")) {
            List<PathListItem> list = new ArrayList<>();
            list.add(new PathListItem("roomA", Optional.empty(), 2));
            list.add(new PathListItem("roomB", Optional.empty(), 2));
            return list;
        }

        if (currentPath.equals("root/roomA/")) {
            List<PathListItem> list = new ArrayList<>();
            int temp = new Random().nextInt();
            list.add(new PathListItem("temp", Optional.of(String.valueOf(temp)), 0));
            list.add(new PathListItem("name", Optional.of("Room A"), 0));
            return list;
        }

        if (currentPath.equals("root/roomB/")) {
            List<PathListItem> list = new ArrayList<>();
            int temp = new Random().nextInt();
            list.add(new PathListItem("temp", Optional.of(String.valueOf(temp)), 0));
            list.add(new PathListItem("name", Optional.of("Room B"), 0));
            return list;
        }

        return List.of();
    }
}
