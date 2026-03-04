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

    private Image getSVGImage(URL url, int width, int height) {
        SVGLoader loader = getInstance().svgLoader;
        SVGDocument document = loader.load(url);

        if (document == null) {
            return null;
        }

        // scale up for better quality when scaling down later
        int scale = 2;
        BufferedImage image = new BufferedImage(width * scale, height * scale, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        document.render(null, g, new ViewBox(0, 0, width * scale, height * scale));

        g.dispose();

        // scale back down to desired size
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaledImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawImage(image, 0, 0, width, height, null);
        g2.dispose();

        return scaledImage;
    }


    public static Icon get(String imageName, Dimension dimension) {
        var instance = getInstance();

        if (!instance.loaded.containsKey(imageName + dimension)) {
            instance.LOGGER.info("loading image %s with dimension %s".formatted(imageName, dimension));
            var image = instance.getSVGImage(IconUtils.class.getResource("/images/" + imageName), dimension.width, dimension.height);

            instance.loaded.put(imageName + dimension, new ImageIcon(image));
        }

        return instance.loaded.get(imageName + dimension);
    }
}
