package com.github.jrohatsch.moqqa.utils;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.ResourceBundle;

public class TextUtils {
    public static String getText(String id) {
        return ResourceBundle.getBundle("texts").getString(id);
    }

}
