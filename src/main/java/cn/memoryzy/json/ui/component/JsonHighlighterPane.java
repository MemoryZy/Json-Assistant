package cn.memoryzy.json.ui.component;

import cn.memoryzy.json.util.UIUtils;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用于在 IntelliJ 插件中显示高亮 JSON 的组件
 */
public class JsonHighlighterPane extends JTextPane {
    
    // 定义 JSON 元素的样式
    private static final Color KEY_COLOR = new JBColor(
        new Color(0x881280),    // 亮色主题颜色
        new Color(0xC792EA)     // 暗色主题颜色
    );
    private static final Color STRING_COLOR = new JBColor(
        new Color(0xC41A16),
        new Color(0xF07178)
    );
    private static final Color NUMBER_COLOR = new JBColor(
        new Color(0x1C00CF),
        new Color(0x82AAFF)
    );
    private static final Color BOOLEAN_COLOR = new JBColor(
        new Color(0x0D22AA),
        new Color(0x7986CB)
    );
    private static final Color NULL_COLOR = new JBColor(
        new Color(0x0000FF),
        new Color(0x7986CB)
    );
    private static final Color COMMENT_COLOR = new JBColor(
        new Color(0x6A737D),
        new Color(0x7C7C7C)
    );
    private static final Color SYMBOL_COLOR = new JBColor(
        new Color(0x000000),
        new Color(0xBBBBBB)
    );
    
    // 样式定义
    private Style keyStyle;
    private Style stringStyle;
    private Style numberStyle;
    private Style booleanStyle;
    private Style nullStyle;
    private Style commentStyle;
    private Style symbolStyle;
    
    public JsonHighlighterPane() {
        setupStyles();
        configureComponent();
    }
    
    public JsonHighlighterPane(@NotNull String json) {
        this();
        setJson(json);
    }
    
    private void setupStyles() {
        StyleContext context = new StyleContext();
        Style defaultStyle = context.getStyle(StyleContext.DEFAULT_STYLE);
        
        // 创建键名样式
        keyStyle = context.addStyle("key", defaultStyle);
        StyleConstants.setForeground(keyStyle, KEY_COLOR);
        StyleConstants.setBold(keyStyle, true);
        
        // 创建字符串值样式
        stringStyle = context.addStyle("string", defaultStyle);
        StyleConstants.setForeground(stringStyle, STRING_COLOR);
        
        // 创建数字值样式
        numberStyle = context.addStyle("number", defaultStyle);
        StyleConstants.setForeground(numberStyle, NUMBER_COLOR);
        
        // 创建布尔值样式
        booleanStyle = context.addStyle("boolean", defaultStyle);
        StyleConstants.setForeground(booleanStyle, BOOLEAN_COLOR);
        
        // 创建null值样式
        nullStyle = context.addStyle("null", defaultStyle);
        StyleConstants.setForeground(nullStyle, NULL_COLOR);
        
        // 创建注释样式
        commentStyle = context.addStyle("comment", defaultStyle);
        StyleConstants.setForeground(commentStyle, COMMENT_COLOR);
        StyleConstants.setItalic(commentStyle, true);
        
        // 创建语法符号样式
        symbolStyle = context.addStyle("symbol", defaultStyle);
        StyleConstants.setForeground(symbolStyle, SYMBOL_COLOR);
    }
    
    private void configureComponent() {
        setEditable(false);
        setFont(UIUtils.consolasFont(14));
        setBackground(UIUtil.getEditorPaneBackground());
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }
    
    /**
     * 设置要显示的 JSON 内容并应用语法高亮
     * 
     * @param json 要显示的 JSON 字符串
     */
    public void setJson(@NotNull String json) {
        try {
            // 清除当前内容
            StyledDocument doc = (StyledDocument) getDocument();
            doc.remove(0, doc.getLength());
            
            // 添加新内容
            doc.insertString(0, json, null);
            
            // 应用语法高亮
            applyHighlighting(json);
        } catch (BadLocationException e) {
            // 处理异常
        }
    }
    
    private void applyHighlighting(String json) {
        StyledDocument doc = (StyledDocument) getDocument();
        
        try {
            // 应用语法符号高亮
            applyStyle(doc, "[{}\\[\\],:]", symbolStyle);
            
            // 应用键名高亮
            applyStyle(doc, "\"(\\w+)\"\\s*:", keyStyle);
            
            // 应用字符串值高亮
            applyStyle(doc, "\"([^\"]*)\"", stringStyle);
            
            // 应用数字值高亮
            applyStyle(doc, "\\b\\d+\\b", numberStyle);
            
            // 应用布尔值高亮
            applyStyle(doc, "\\b(true|false)\\b", booleanStyle);
            
            // 应用null值高亮
            applyStyle(doc, "\\b(null)\\b", nullStyle);
            
            // 应用注释高亮
            applyStyle(doc, "//.*", commentStyle);
            
        } catch (BadLocationException e) {
            // 处理异常
        }
    }
    
    private void applyStyle(StyledDocument doc, String regex, Style style) throws BadLocationException {
        String text = doc.getText(0, doc.getLength());
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            doc.setCharacterAttributes(start, end - start, style, false);
        }
    }
    
    /**
     * 创建带滚动条的预览面板
     * 
     * @param width 面板宽度
     * @param height 面板高度
     * @return 配置好的滚动面板
     */
    public JScrollPane createScrollPane(int width, int height) {
        JBScrollPane scrollPane = new JBScrollPane(this);
        scrollPane.setPreferredSize(new Dimension(width, height));
        scrollPane.setMinimumSize(new Dimension(width / 2, height / 2));
        return scrollPane;
    }
    
    /**
     * 创建带滚动条的预览面板（默认尺寸）
     * 
     * @return 配置好的滚动面板
     */
    public JScrollPane createScrollPane() {
        return createScrollPane(400, 200);
    }
    
    // public static void main(String[] args) {
    //     // 示例用法
    //     JFrame frame = new JFrame("JSON Highlighter Demo");
    //     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //     frame.setSize(600, 400);
    //
    //     // 创建 JSON 内容
    //     String json = "{\n" +
    //                  "  // 用户信息\n" +
    //                  "  \"id\": 12345,\n" +
    //                  "  \"name\": \"张三\",\n" +
    //                  "  \"active\": true,\n" +
    //                  "  \"roles\": [\"admin\", \"user\"],\n" +
    //                  "  \"contact\": {\n" +
    //                  "    \"email\": \"zhangsan@example.com\",\n" +
    //                  "    \"phone\": null\n" +
    //                  "  }\n" +
    //                  "}";
    //
    //     // 创建高亮组件
    //     JsonHighlighterPane highlighter = new JsonHighlighterPane(json);
    //
    //     // 添加到框架
    //     frame.add(highlighter.createScrollPane());
    //     frame.setLocationRelativeTo(null);
    //     frame.setVisible(true);
    // }
}