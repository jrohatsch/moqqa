package com.github.jrohatsch.moqqa.ui;

import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.domain.Message;
import com.github.jrohatsch.moqqa.utils.IconUtils;
import com.github.jrohatsch.moqqa.utils.TextUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class DeleteButton extends JButton {
    private final Logger LOGGER = Logger.getLogger(getClass().getSimpleName());

    private final ExecutorService executorService;

    public DeleteButton(Datahandler datahandler, Supplier<String> topicSupplier, JComponent toolTipParent) {
        executorService = Executors.newSingleThreadExecutor();


        setIcon(IconUtils.get("trash.svg", new Dimension(18,18)));

        final Color defaultBackground = getBackground();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);

                setBackground(Color.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                setBackground(defaultBackground);
            }
        });


        setToolTipText(TextUtils.getText("tooltip.deleteButton"));

        addActionListener(a -> {
            final String topic = topicSupplier.get();

            Predicate<Message> isTopic = message -> message.topic().equals(topic);
            Predicate<Message> isChildTopic = message -> message.topic().startsWith(topic.concat("/"));

            List<Message> messagesToDelete = datahandler.getMessages(isTopic.or(isChildTopic));

            LOGGER.info("Deleting %d messages".formatted(messagesToDelete.size()));

            var popupWhileDeleting = ToolTipPopup.bottomRightCorner(toolTipParent, TextUtils.getText("tooltip.whileDelete").formatted(messagesToDelete.size()), Duration.ZERO);

            executorService.submit(() -> {
                messagesToDelete.forEach(message -> {
                    datahandler.connector().publish(message.topic(), "", true);
                });
                popupWhileDeleting.hide();
                ToolTipPopup.bottomRightCorner(toolTipParent, TextUtils.getText("tooltip.afterDelete").formatted(messagesToDelete.size()), Duration.ofSeconds(3));
            });
        });
    }

}
