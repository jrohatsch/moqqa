package com.github.jrohatsch.moqqa.utils;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.parser.SVGLoader;

import javax.swing.*;
import java.awt.*;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class IconUtils {
    private final Logger LOGGER = Logger.getLogger(getClass().getSimpleName());
    private static IconUtils instance;
    private Map<String, Icon> loaded;
    private SVGLoader svgLoader;

    private static IconUtils getInstance() {
        if (instance == null) {
            instance = new IconUtils();
            instance.loaded = new HashMap<>();
            instance.svgLoader = new SVGLoader();
        }
        return instance;
    }

    public static Image getSVGImage(URL url, int width, int height) {
        SVGLoader loader = getInstance().svgLoader;
        SVGDocument document = loader.load(url);

        if (document == null) {
            return null;
        }

        // create empty transparent image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        // render svg element on image
        document.render(null, g, new ViewBox(0,0, width,height));

        g.dispose();
        return image;
    }

    public static Icon get(String imageName, Dimension dimension) {
        var instance = getInstance();

        if (instance.loaded.containsKey(imageName + dimension)) {
            return instance.loaded.get(imageName + dimension);
        }

        instance.LOGGER.info("loading image %s with dimension %s".formatted(imageName, dimension));
        var image = getSVGImage(IconUtils.class.getResource("/images/" + imageName), 24, 24);

        instance.loaded.put(imageName + dimension, new ImageIcon(image));

        return instance.loaded.get(imageName);
    }
}
