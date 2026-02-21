package com.github.jrohatsch.moqqa.utils;

import java.util.ResourceBundle;

public class TextUtils {
    public static String getText(String id) {
        return ResourceBundle.getBundle("texts").getString(id);
    }

}
