package com.github.jrohatsch.moqqa.ui;

import javax.swing.*;

public class SearchPathButton extends JButton {
    private static final int MAX_PATH_LENGTH = 37;
    private final String path;

    public SearchPathButton(String path) {
        super();
        this.path = path;
        if (path.length() > MAX_PATH_LENGTH) {
            super.setText(path.substring(0, 36).concat("..."));
        } else {
            super.setText(path);
        }
    }

    public String getPath() {
        return path;
    }
}
