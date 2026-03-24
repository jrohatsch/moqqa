package com.github.jrohatsch.moqqa.ui;

import com.github.jrohatsch.moqqa.session.AppConfig;
import com.github.jrohatsch.moqqa.session.AppConfigHandler;
import com.github.jrohatsch.moqqa.utils.AppRestarter;

import javax.swing.*;

public class ScaleUIButton extends JButton {
    public ScaleUIButton(double scaleFactor, AppConfigHandler appConfigHandler) {
        super();
        int percentage = (int) (scaleFactor * 100);
        setText("%s%%".formatted(percentage));

        addActionListener(a -> {
            if(appConfigHandler.loadConfig().scalingFactor() != scaleFactor) {
                // change scaling factor
                appConfigHandler.saveConfig(new AppConfig(scaleFactor));
                // Safely restart the whole application
                AppRestarter.restartApplication();
            }
        });

        if (appConfigHandler.loadConfig().scalingFactor() == scaleFactor) {
            setBackground(UIManager.getColor("Button.select"));
        }
    }
}
