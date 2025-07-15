// package cn.memoryzy.json.ui.dialog;
//
// import cn.memoryzy.json.enums.JsonTreeNodeType;
// import cn.memoryzy.json.ui.node.JsonTreeNode;
// import com.intellij.openapi.project.Project;
// import com.intellij.openapi.ui.DialogWrapper;
// import com.intellij.ui.components.JBLabel;
// import com.intellij.util.ui.JBUI;
//
// import javax.swing.*;
// import java.awt.*;
//
// public class ModifyNodeDialog extends DialogWrapper {
//     private final JsonTreeNode node;
//     private final boolean allowKeyEdit;
//     private  JCheckBox applyToSourceCheckBox;
//     private  JTextField keyField;
//     // private  JsonValueEditor valueEditor;
//
//     public ModifyNodeDialog(JsonTreeNode node, Project project) {
//         super(project, true);
//         this.node = node;
//         this.allowKeyEdit = node.getNodeType() == JsonTreeNodeType.JSONObjectProperty;
//
//         setTitle("修改JSON节点");
//         init();
//     }
//
//     @Override
//     protected JComponent createCenterPanel() {
//         JPanel mainPanel = new JPanel(new GridBagLayout());
//         GridBagConstraints gbc = new GridBagConstraints();
//         gbc.insets = JBUI.insets(5);
//         gbc.fill = GridBagConstraints.HORIZONTAL;
//         gbc.weightx = 1;
//
//         // 键编辑区域（仅对象属性可编辑）
//         if (allowKeyEdit) {
//             gbc.gridy = 0;
//             gbc.gridx = 0;
//             mainPanel.add(new JBLabel("键:"), gbc);
//
//             gbc.gridx = 1;
//             keyField = new JTextField(String.valueOf(node.getUserObject()));
//             mainPanel.add(keyField, gbc);
//         } else {
//             keyField = null;
//         }
//
//         // 值编辑区域（所有节点可编辑）
//         gbc.gridy++;
//         gbc.gridx = 0;
//         mainPanel.add(new JBLabel("值:"), gbc);
//
//         gbc.gridx = 1;
//         valueEditor = new JsonValueEditor(node.getValue());
//         mainPanel.add(valueEditor.getComponent(), gbc);
//
//         // 源文件应用选项
//         gbc.gridy++;
//         gbc.gridx = 0;
//         gbc.gridwidth = 2;
//         applyToSourceCheckBox = new JCheckBox("将修改应用到源文件");
//         applyToSourceCheckBox.setSelected(true);
//         mainPanel.add(applyToSourceCheckBox, gbc);
//
//         return mainPanel;
//     }
//
//     @Override
//     protected void doOKAction() {
//         super.doOKAction();
//
//         // 应用修改到树节点
//         if (allowKeyEdit && !keyField.getText().isEmpty()) {
//             node.setUserObject(keyField.getText());
//         }
//
//         node.setValue(valueEditor.getParsedValue());
//         close(OK_EXIT_CODE);
//     }
//
//     public boolean shouldApplyToSource() {
//         return applyToSourceCheckBox.isSelected();
//     }
//
//     public String getNewKey() {
//         return allowKeyEdit ? keyField.getText() : null;
//     }
//
//     public Object getNewValue() {
//         return valueEditor.getParsedValue();
//     }
// }