package com.github.jrohatsch.moqqa.ui;

import com.github.jrohatsch.moqqa.session.AppConfig;
import com.github.jrohatsch.moqqa.session.AppConfigHandler;

import javax.swing.*;

public class ScaleUIButton extends JButton {
    public ScaleUIButton(double scaleFactor, AppConfigHandler appConfigHandler) {
        super();
        int percentage = (int) (scaleFactor * 100);
        setText("%s%%".formatted(percentage));
        addActionListener(a -> {
            appConfigHandler.saveConfig(new AppConfig(scaleFactor));
        });
    }
}
