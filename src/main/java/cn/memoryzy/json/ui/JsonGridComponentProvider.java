package cn.memoryzy.json.ui;

import cn.hutool.core.collection.CollUtil;
import cn.memoryzy.json.model.wrapper.ArrayWrapper;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;
import cn.memoryzy.json.ui.component.ModernTable;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.UIUtils;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 简单的表格展示
 *
 * @author Memory
 * @since 2025/5/21
 */
public class JsonGridComponentProvider {

    private ModernTable table;
    private JPanel tableComponent;
    private final boolean chineseLocale;
    private final List<ImmutablePair<Integer, Integer>> unset = new ArrayList<>();

    public JsonGridComponentProvider(JsonWrapper wrapper) {
        this.chineseLocale = PlatformUtil.isChineseLocale();
        init(wrapper);
    }

    private void init(JsonWrapper wrapper) {
        table = new ModernTable(createTableModel(wrapper));
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table)
                .setPanelBorder(JBUI.Borders.empty())
                .setScrollPaneBorder(JBUI.Borders.empty());
        tableComponent = new BorderLayoutPanel().addToCenter(decorator.createPanel());
    }

    private DefaultTableModel createTableModel(JsonWrapper wrapper) {
        if (null == wrapper) return new DefaultTableModel();

        return wrapper.isObject()
                // 如果是单独的对象，那么 列 1 就是标题，列 2 就是值
                ? createObjectTable((ObjectWrapper) wrapper)
                // 如果是数组，那么就是多列，第一行作为标题
                : createArrayTable((ArrayWrapper) wrapper);
    }

    public void rebuildTable(JsonWrapper wrapper) {
        table.setModel(createTableModel(wrapper));
        table.resizeColumns();
        UIUtils.repaintComponent(table);
    }

    private DefaultTableModel createObjectTable(ObjectWrapper wrapper) {
        String[] columns = {" ", "Key", "Value"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 返回false来禁止整行编辑
                return false;
            }
        };

        int i = 0;
        for (Map.Entry<String, Object> entry : wrapper.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String valueStr = String.valueOf(value);

            if (value instanceof ObjectWrapper || value instanceof ArrayWrapper) {
                valueStr = JsonUtil.compressJson(value);
            }

            // 添加一行：键和对应的值
            tableModel.addRow(new Object[]{i, key, valueStr});

            i++;
        }

        return tableModel;
    }

    private DefaultTableModel createArrayTable(ArrayWrapper wrapper) {
        if (CollUtil.isEmpty(wrapper)) {
            return new DefaultTableModel();
        }

        // 统计对象
        List<ObjectWrapper> objs = wrapper.stream()
                .filter(el -> el instanceof ObjectWrapper)
                .map(el -> (ObjectWrapper) el)
                .collect(Collectors.toList());

        String[] columns;
        String[][] data;
        // 如果数组中不全是对象，还存在普通类型，与对象平级，那么则都显示为 toString
        if (objs.size() < wrapper.size()) {
            columns = new String[]{" ", chineseLocale ? "列0" : "C0"};
            data = new String[wrapper.size()][2];

            for (int i = 0; i < wrapper.size(); i++) {
                Object obj = wrapper.get(i);
                String objStr = String.valueOf(obj);

                if (obj instanceof ObjectWrapper || obj instanceof ArrayWrapper) {
                    objStr = JsonUtil.compressJson(obj);
                }

                data[i] = new String[]{String.valueOf(i), objStr};
            }

        } else {
            // 如果数组中有多个对象，且对象不一致，则选择字段最多的对象展示，其余无此字段的，显示 <unset>
            ObjectWrapper obj = (ObjectWrapper) wrapper.get(0);
            String[] originalColumns = obj.keySet().toArray(new String[0]);
            columns = new String[originalColumns.length + 1];
            columns[0] = " ";
            System.arraycopy(originalColumns, 0, columns, 1, originalColumns.length);
            data = new String[wrapper.size()][columns.length];

            for (int i = 0; i < wrapper.size(); i++) {
                ObjectWrapper objectWrapper = (ObjectWrapper) wrapper.get(i);
                String[] row = new String[columns.length];
                row[0] = Integer.toString(i);

                for (int j = 1; j < columns.length; j++) {
                    // 若后续 Map 缺少键，则值为 unset
                    String key = columns[j];
                    if (!objectWrapper.containsKey(key)) {
                        // 行，列
                        unset.add(ImmutablePair.of(i, j));
                    }

                    Object object = objectWrapper.get(key);
                    String objStr = String.valueOf(object);
                    if (object instanceof ObjectWrapper || object instanceof ArrayWrapper) {
                        objStr = JsonUtil.compressJson(obj);
                    }

                    row[j] = objStr;
                }

                data[i] = row;
            }
        }

        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 返回false来禁止整行编辑
                return false;
            }
        };

        tableModel.setDataVector(data, columns);
        return tableModel;
    }

    public JBTable getTable() {
        return table;
    }

    public JPanel getTableComponent() {
        return tableComponent;
    }
}
