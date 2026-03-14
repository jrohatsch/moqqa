package com.github.jrohatsch.moqqa.ui;

import com.github.jrohatsch.moqqa.session.SessionHandler;
import com.github.jrohatsch.moqqa.utils.TextUtils;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class SessionTabMouseListener implements MouseListener {
    private final SessionHandler sessionHandler;

    public SessionTabMouseListener(SessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            var tabbedPane = (JTabbedPane) e.getSource();
            var index = tabbedPane.getSelectedIndex();
            var menu = new JPopupMenu("");
            var closeTab = new JMenuItem(TextUtils.getText("tab.close"));
            closeTab.addActionListener(a -> {
                if (tabbedPane.getTabCount() > 2) {
                    if (index == 0) {
                        // if the first tab is selected move index to the next one
                        tabbedPane.setSelectedIndex(1);
                    } else {
                        // generally move index to the previous one
                        tabbedPane.setSelectedIndex(index - 1);
                    }
                    sessionHandler.delete(tabbedPane.getTitleAt(index));
                    tabbedPane.removeTabAt(index);
                }
            });
            var closeOtherTabs = new JMenuItem(TextUtils.getText("tab.closeOther"));
            closeOtherTabs.addActionListener(a -> {
                String tabToKeep = tabbedPane.getTitleAt(index);
                int tabRemoveIndex = 0;
                while (tabbedPane.getTabCount() > 2) {
                    String eachTab = tabbedPane.getTitleAt(tabRemoveIndex);
                    if(eachTab.equals(tabToKeep)) {
                        // ignore this index step to next one
                        tabRemoveIndex++;
                    } else {
                        sessionHandler.delete(tabbedPane.getTitleAt(tabRemoveIndex));
                        tabbedPane.removeTabAt(tabRemoveIndex);
                        // do not increment index, as other tabs to close automatically shift
                    }
                }
            });

            // check if more than the tabs ["Session", " + "] are present
            if (tabbedPane.getTabCount() > 2) {
                menu.add(closeTab);
                menu.add(closeOtherTabs);
                menu.show(tabbedPane, e.getX(), e.getY());
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
