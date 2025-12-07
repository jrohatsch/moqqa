package com.github.jrohatsch.moqqa.utils;

import java.awt.*;

public class ColorUtils {
    public static Color rgb(int red, int green, int blue) {
        var hsb = Color.RGBtoHSB(red, green, blue, null);
        return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    }
}
