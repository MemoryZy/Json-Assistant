package cn.memoryzy.json.ui.listener;

import cn.memoryzy.json.model.HistoryEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/11/29
 */
public class ListRightClickPopupMenuMouseAdapter extends MouseAdapter {

    private final JList<HistoryEntry> showList;
    private final JPopupMenu popupMenu;

    public ListRightClickPopupMenuMouseAdapter(JList<HistoryEntry> showList, JPopupMenu popupMenu) {
        this.showList = showList;
        this.popupMenu = popupMenu;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            int x = e.getX();
            int y = e.getY();

            HistoryEntry selectedValue = showList.getSelectedValue();
            if (Objects.isNull(selectedValue)) {
                int index = showList.locationToIndex(new Point(x, y));
                if (index != -1) {
                    showList.setSelectedIndex(index);
                    popupMenu.show(showList, x, y);
                }
            } else {
                popupMenu.show(showList, x, y);
            }
        }
    }
}
