package com.github.jrohatsch.moqqa.components;


import com.github.jrohatsch.moqqa.domain.PathListItem;
import com.github.jrohatsch.moqqa.utils.IconUtils;

import javax.swing.*;
import java.awt.*;

public class PathItemRenderer extends DefaultListCellRenderer {

    private String mapImage(PathListItem item) {
        return switch (item.type()) {
            case PARENT -> "folder-regular-full.svg";
            case TEXT -> "bars-staggered-solid-full.svg";
            case BOOL -> "square-binary-solid-full.svg";
            case NUMBER -> "chart-line-solid-full.svg";
        };
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
