package com.github.jrohatsch.moqqa.utils;

import java.awt.*;

public class ColorUtils {
    public static final Color RED = Color.RED;
    public static Color BLUE = rgb(64, 111, 180);

    private static Color rgb(int red, int green, int blue) {
        var hsb = Color.RGBtoHSB(red, green, blue, null);
        return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    }
}
