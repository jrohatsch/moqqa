package com.github.jrohatsch.moqqa.swingworkers;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class SearchPathUpdater extends SwingWorker {
    private JToolBar path;

    private final Consumer<String> callbackPathChange;

    public SearchPathUpdater(Consumer<String> callbackPathChange){
        path = new JToolBar();
        add(">");
        this.callbackPathChange = callbackPathChange;
    }

    @Override
    protected Object doInBackground() throws Exception {
        return null;
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
                JButton button = (JButton) component;
                buffer = buffer.concat(button.getText());
                buffer = buffer.concat("/");
            } catch (ClassCastException ignored) {

            }

        }

        return buffer;
    }

    private void stepToIndex(int index) {
        Component[] components = path.getComponents();
        path.removeAll();
        for(int i=0; i<=index; ++i) {
            path.add(components[i]);
        }
        path.updateUI();
        callbackPathChange.accept(getPath());
    }

    public void add(String subPath) {
        if (path.getComponentCount() > 1) {
            path.addSeparator();
        }
        var button = new JButton(subPath);
        final int index = path.getComponentCount();
        button.addActionListener(action -> {
            System.out.println("element "+ index + " was clicked");
            stepToIndex(index);
        });
        path.add(button);
        path.updateUI();
    }

    public JToolBar getToolbar() {
        return path;
    }

    public void clear() {
        path.removeAll();
        add(">");
        path.updateUI();
    }
}
