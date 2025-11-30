package com.github.jrohatsch.moqqa.utils;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.ResourceBundle;

public class TextUtils {
    public static String getText(String id) {
        return ResourceBundle.getBundle("texts").getString(id);
    }

    public static void setSize(int size){
        UIManager.put("Label.font", new FontUIResource(new Font("Default", Font.PLAIN, size)));
        UIManager.put("Button.font", new FontUIResource(new Font("Default", Font.BOLD, size)));
        UIManager.put("TextField.font", new FontUIResource(new Font("Default", Font.PLAIN, size)));
        UIManager.put("ComboBox.font", new FontUIResource(new Font("Default", Font.PLAIN, size)));
        UIManager.put("CheckBox.font", new FontUIResource(new Font("Default", Font.PLAIN, size)));
        UIManager.put("TabbedPane.font", new FontUIResource(new Font("Default", Font.PLAIN, size)));
    }
}
