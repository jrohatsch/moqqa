package com.github.jrohatsch.moqqa.utils;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class IconUtils {
    private static IconUtils instance;
    private Map<String, Icon> loaded;

    private static IconUtils getInstance() {
        if (instance == null) {
            instance = new IconUtils();
            instance.loaded = new HashMap<>();
        }
        return instance;
    }

    private String absolutePath(String imageName) {
        URL url = IconUtils.class.getResource("/images/%s".formatted(imageName));
        if (url == null) {
            throw new RuntimeException("Could not find image %s!".formatted(imageName));
        }
        return url.getPath();
    }

    public static Icon get(String imageName, Dimension dimension) {
        var instance = getInstance();

        if (instance.loaded.containsKey(imageName + dimension)) {
            return instance.loaded.get(imageName + dimension);
        }

        System.out.printf("loading image %s with dimension %s%n",imageName, dimension);
        Image image = new ImageIcon(instance.absolutePath(imageName)).getImage();
        image = image.getScaledInstance(dimension.width,dimension.height,0);

        instance.loaded.put(imageName + dimension, new ImageIcon(image));

        return instance.loaded.get(imageName);
    }
}
