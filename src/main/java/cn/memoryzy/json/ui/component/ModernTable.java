package cn.memoryzy.json.ui.component;

import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;
import java.awt.*;

/**
 * @author Memory
 * @since 2025/5/26
 */
public class ModernTable extends JBTable {

    private static final Border CELL_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 1, 1, JBColor.GRAY), // 外边框
            BorderFactory.createEmptyBorder(2, 5, 2, 5) // 内边距
    );

    public ModernTable(TableModel model) {
        super(model);
        init();
    }

    private void init() {
        initStyle();
        initWidth();
    }

    public void initWidth() {
        // 获取列模型
        TableColumnModel columnModel = getColumnModel();
        if (columnModel.getColumnCount() == 0) {
            return;
        }

        // 1. 设置序号列固定宽度
        TableColumn noColumn = columnModel.getColumn(0);
        noColumn.setMinWidth(30);   // 最小宽度
        noColumn.setMaxWidth(50);   // 最大宽度
        noColumn.setPreferredWidth(40); // 理想宽度

        // 2. 配置其他列自动调整
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); // 自动调整所有列

        // 设置最后一列自动填充剩余空间
        setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    }

    private void initStyle() {
        // 基础样式配置
        setShowGrid(false);
        setRowHeight(35);
        setIntercellSpacing(new Dimension(0, 0));
        setSelectionBackground(new JBColor(new Color(220, 240, 255), new Color(70, 130, 180)));
        setSelectionForeground(JBColor.DARK_GRAY);

        // 自定义渲染器
        setDefaultRenderer(Object.class, new ModernTableCellRenderer());

        // 表头样式
        JTableHeader header = getTableHeader();
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        header.setBackground(new JBColor(Gray._239, new Color(45, 45, 47)));
        header.setForeground(new JBColor(Gray._80, new Color(188, 190, 196)));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new JBColor(Gray._220, new Color(45, 45, 47))));
        header.setPreferredSize(new Dimension(header.getWidth(), 45));
    }

    static class ModernTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            ((JComponent) c).setBorder(CELL_BORDER);

            if (column == 0) {
                ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
            }

            setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

            return c;
        }

        @Override
        protected void paintComponent(Graphics g) {
            // 圆角背景
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(getBackground());
            g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            super.paintComponent(g);
            g2d.dispose();
        }
    }
}
