package com.github.jrohatsch.moqqa.components;


import com.github.jrohatsch.moqqa.domain.PathListItem;
import com.github.jrohatsch.moqqa.utils.IconUtils;
import org.apache.commons.lang3.math.NumberUtils;
import tools.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.*;

public class PathItemRenderer extends DefaultListCellRenderer {

    private String mapImage(PathListItem item) {
        if (item.childTopics() > 0) {
            return "cube-solid-full-48x48.png";
        } else if (item.value().isPresent()) {
            String value = item.value().get();

            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                return "code-commit-solid-full-48x48.png";
            } else if (NumberUtils.isCreatable(value)) {
                return "wave-square-solid-full-48x48.png";
            }
        }

        return "align-left-solid-full-48x48.png";
    }

    @Override
    public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus)
    {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        String image = mapImage((PathListItem) value);

        setIcon(IconUtils.get(image, new Dimension(24,24)));

        return label;
    }

}
