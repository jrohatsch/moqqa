package com.github.jrohatsch.moqqa.ui;

import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.domain.Message;
import com.github.jrohatsch.moqqa.utils.ColorUtils;
import com.github.jrohatsch.moqqa.utils.TextUtils;

import javax.swing.*;
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

        // Wastebasket unicode
        setText(Character.toString(128465));
        setBackground(ColorUtils.RED);

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
