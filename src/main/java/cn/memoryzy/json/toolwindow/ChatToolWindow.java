package cn.memoryzy.json.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatToolWindow implements ToolWindowFactory {

    private JTextPane chatArea;
    private JTextField inputField;
    private StyledDocument doc;
    private Style userStyle;
    private Style aiStyle;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 创建聊天区域
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        doc = chatArea.getStyledDocument();
        
        // 初始化样式
        initStyles();
        
        JBScrollPane scrollPane = new JBScrollPane(chatArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 输入面板
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        JButton sendButton = new JButton("发送");
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        // 发送按钮事件
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = inputField.getText().trim();
                if (!text.isEmpty()) {
                    addMessage(text, true);  // 用户消息
                    processAIResponse(text); // 处理AI响应
                    inputField.setText("");
                }
            }
        });

        // 回车键发送
        inputField.addActionListener(e -> sendButton.doClick());

        // 将面板添加到工具窗口
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private void initStyles() {
        // 用户消息样式（右对齐）
        userStyle = doc.addStyle("UserStyle", null);
        StyleConstants.setAlignment(userStyle, StyleConstants.ALIGN_RIGHT);
        StyleConstants.setForeground(userStyle, Color.WHITE);
        StyleConstants.setBackground(userStyle, new Color(0, 120, 215));
        StyleConstants.setBold(userStyle, true);
        StyleConstants.setLeftIndent(userStyle, 50);

        // AI消息样式（左对齐）
        aiStyle = doc.addStyle("AIStyle", null);
        StyleConstants.setAlignment(aiStyle, StyleConstants.ALIGN_LEFT);
        StyleConstants.setForeground(aiStyle, Color.BLACK);
        StyleConstants.setBackground(aiStyle, new Color(240, 240, 240));
        StyleConstants.setBold(aiStyle, false);
    }

    private void addMessage(String message, boolean isUser) {
        try {
            // 添加消息
            doc.insertString(doc.getLength(), message + "\n", isUser ? userStyle : aiStyle);
            
            // 自动滚动到底部
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void processAIResponse(String userInput) {
        // 这里模拟AI响应，实际应替换为API调用
        String response = "已收到你的消息: " + userInput + "\n（这是模拟的AI回复）";
        
        // 添加延迟效果
        Timer timer = new Timer(1000, e -> {
            addMessage(response, false);
        });
        timer.setRepeats(false);
        timer.start();
    }
}