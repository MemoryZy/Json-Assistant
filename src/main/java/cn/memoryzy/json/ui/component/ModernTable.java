package cn.memoryzy.json.ui.component;

import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;

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
        resizeColumns();
    }

    public void resizeColumns() {
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
        setShowGrid(true);
        setRowHeight(35);
        setIntercellSpacing(new Dimension(0, 0));
        setSelectionBackground(new JBColor(new Color(220, 240, 255), new Color(70, 130, 180)));
        setSelectionForeground(JBColor.DARK_GRAY);

        // 自定义渲染器
        setDefaultRenderer(Object.class, new ModernTableCellRenderer());

        // 4. 设置序号列（第一列）为小宽度+灰色
        setupSequenceColumn();

        // 表头样式
        JTableHeader header = getTableHeader();
        header.setReorderingAllowed(true);
        header.setBorder(JBUI.Borders.empty(0,1,1,1));
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        header.setBackground(new JBColor(Gray._239, new Color(69, 72, 74)));
        header.setPreferredSize(new Dimension(header.getWidth(), 45));
    }


    private void setupSequenceColumn() {
        if (getColumnCount() == 0) return;

        // 应用渲染器到序号列
        getColumnModel().getColumn(0).setCellRenderer(createSequenceRenderer());

        // 3. 强制刷新组件
        revalidate();
        repaint();
        if (getTableHeader() != null) {
            getTableHeader().repaint();
        }
    }


    private TableCellRenderer createSequenceRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {

                // 调用父类方法创建默认组件
                Component comp = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                // 仅针对序号列进行处理
                if (column == 0) {
                    // 强制居中
                    ((JLabel) comp).setHorizontalAlignment(SwingConstants.CENTER);

                    // 强制设置灰色 - 使用固定值确保生效
                    comp.setForeground(JBColor.GRAY);
                }

                return comp;
            }
        };
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
