// package cn.memoryzy.json.ui.dialog;
//
// import javax.swing.*;
// import java.awt.*;
//
// class JsonValueEditor {
//     private final CardLayout cardLayout = new CardLayout();
//     private final JPanel container = new JPanel(cardLayout);
//     private final JTextField textField = new JTextField();
//     private final JComboBox<Boolean> booleanCombo = new JComboBox<>(new Boolean[]{true, false});
//     private final JSpinner numberSpinner = new JSpinner(new SpinnerNumberModel());
//     private final JLabel readOnlyLabel = new JLabel();
//
//     private Object originalValue;
//     private String currentType;
//
//     public JsonValueEditor(Object value) {
//         this.originalValue = value;
//         setupUI();
//     }
//
//     private void setupUI() {
//         // 确定值类型并显示相应编辑器
//         if (value == null) {
//             readOnlyLabel.setText("null");
//             showComponent("null");
//         } else if (value instanceof String) {
//             textField.setText((String) value);
//             showComponent("string");
//         } else if (value instanceof Boolean) {
//             booleanCombo.setSelectedItem(value);
//             showComponent("boolean");
//         } else if (value instanceof Number) {
//             numberSpinner.setValue(value);
//             showComponent("number");
//         } else {
//             readOnlyLabel.setText(value.getClass().getSimpleName() + " (复杂类型)");
//             showComponent("complex");
//         }
//     }
//
//     private void showComponent(String type) {
//         currentType = type;
//         cardLayout.show(container, type);
//     }
//
//     public JComponent getComponent() {
//         return container;
//     }
//
//     public Object getParsedValue() {
//         switch (currentType) {
//             case "string":
//                 return textField.getText();
//             case "boolean":
//                 return booleanCombo.getSelectedItem();
//             case "number":
//                 return numberSpinner.getValue();
//             case "null":
//                 return null;
//             default:
//                 return originalValue; // 复杂类型不修改
//         }
//     }
// }