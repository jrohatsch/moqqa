package com.github.jrohatsch.moqqa.components;

import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.ui.SearchPathButton;

import javax.swing.*;
import java.awt.*;

public class SearchPathUpdater {
    private final Datahandler datahandler;
    private final JToolBar path;

    public SearchPathUpdater(Datahandler datahandler) {
        path = new JToolBar();
        this.datahandler = datahandler;
        add(">");
    }

    public String getPath() {
        if (path.getComponents().length == 0) {
            return "";
        }
        String buffer = "";

        boolean ignoreFirst = true;
        for (Component component : path.getComponents()) {
            if (ignoreFirst) {
                ignoreFirst = false;
                continue;
            }
            try {
                SearchPathButton button = (SearchPathButton) component;
                buffer = buffer.concat(button.getPath());
                buffer = buffer.concat("/");
            } catch (ClassCastException ignored) {

            }

        }

        return buffer;
    }

    private void stepToIndex(int index) {
        Component[] components = path.getComponents();
        path.removeAll();
        for (int i = 0; i <= index; ++i) {
            path.add(components[i]);
        }
        path.updateUI();
        datahandler.setSearchPath(getPath());
    }

    public void add(String subPath) {
        add(subPath, null);
    }

    public void add(String subPath, String tooltipText) {
        if (path.getComponentCount() > 0) {
            path.addSeparator();
        }
        var button = new SearchPathButton(subPath);

        if (tooltipText != null) {
            button.setToolTipText(tooltipText);
        }

        final int index = path.getComponentCount();
        button.addActionListener(action -> stepToIndex(index));
        button.setFont(path.getFont());
        path.add(button);
        path.updateUI();
    }

    public JToolBar getToolbar() {
        return path;
    }

    public void pop() {
        if (path.getComponentCount() >= 3) {
            // remove last button and separator
            path.remove(path.getComponentCount() - 1);
            path.remove(path.getComponentCount() - 1);
            path.updateUI();
        } else {
            System.out.println("already at root level!");
        }
    }
}
