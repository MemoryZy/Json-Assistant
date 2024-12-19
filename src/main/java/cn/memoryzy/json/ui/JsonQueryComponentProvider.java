package cn.memoryzy.json.ui;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.enums.JsonQuerySchema;
import cn.memoryzy.json.ui.editor.SearchTextField;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.apache.commons.lang3.ArrayUtils;

import javax.swing.*;
import java.awt.*;

/**
 * @author Memory
 * @since 2024/12/17
 */
public class JsonQueryComponentProvider implements Disposable {

    public static final String JSON_PATH_HISTORY_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".JsonPathHistory";
    public static final String JMES_PATH_HISTORY_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".JmesPathHistory";
    public static final String SPLITTER_PROPORTION_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".SplitterProportionKey";

    private final Project project;
    private final JBLabel searchLabel;
    private final ComboBox<JsonQuerySchema> searchChooser;
    private final SearchTextField searchTextField;
    private final JBPanelWithEmptyText resultWrapper;
    private final JBLabel resultLabel;
    private final Editor resultEditor;
    private final JBTextArea errorOutputArea;
    private final JBScrollPane errorOutputContainer;
    private final JBLabel docLabel;
    private final Editor docEditor;

    public JsonQueryComponentProvider(Project project) {
        this.project = project;
        this.searchLabel = new JBLabel(JsonAssistantBundle.messageOnSystem("json.query.search"));
        this.searchChooser = new ComboBox<>();
        this.searchTextField = new SearchTextField(JSON_PATH_HISTORY_KEY, 10, this::evaluate);

        this.resultWrapper = new JBPanelWithEmptyText(new BorderLayout());
        this.resultLabel = new JBLabel(JsonAssistantBundle.messageOnSystem("json.query.evaluate.result"));
        this.resultEditor = createJsonEditor("result.json5", true, EditorKind.PREVIEW);

        this.errorOutputArea = new JBTextArea();
        this.errorOutputContainer = new JBScrollPane(errorOutputArea);

        this.docLabel = new JBLabel(JsonAssistantBundle.messageOnSystem("json.query.evaluate.doc"));
        this.docEditor = createJsonEditor("doc.json5", false, EditorKind.MAIN_EDITOR);
    }

    public JComponent createComponent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createFirstComponent(), BorderLayout.NORTH);
        panel.add(createSecondComponent(), BorderLayout.CENTER);
        return panel;
    }


    private JComponent createFirstComponent() {
        for (JsonQuerySchema value : JsonQuerySchema.values()) {
            searchChooser.addItem(value);
        }

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(searchLabel);
        panel.add(searchChooser);

        return new BorderLayoutPanel()
                .addToTop(panel)
                .addToCenter(searchTextField);
    }

    private JComponent createSecondComponent() {
        // 一个Json原文编辑器（默认颜色），一个计算结果编辑器（跟随主界面）
        JBSplitter splitter = new JBSplitter(true, 0.5f);
        // 保存拆分比例
        splitter.setSplitterProportionKey(SPLITTER_PROPORTION_KEY);

        resultWrapper.getEmptyText().setText(JsonAssistantBundle.messageOnSystem("json.query.evaluate.no.result"));
        resultLabel.setBorder(JBUI.Borders.empty(3, 6));
        resultEditor.setBorder(JBUI.Borders.customLine(JBColor.border(), 1, 0, 0, 0));

        errorOutputArea.setEditable(false);
        errorOutputArea.setWrapStyleWord(true);
        errorOutputArea.setLineWrap(true);
        errorOutputArea.setBorder(JBUI.Borders.empty(10));
        errorOutputContainer.setBorder(JBUI.Borders.customLine(JBColor.border(), 1, 0, 0, 0));

        docLabel.setBorder(JBUI.Borders.empty(3, 6));
        docEditor.setBorder(JBUI.Borders.customLine(JBColor.border(), 1, 0, 0, 0));

        splitter.setFirstComponent(resultWrapper);
        splitter.setSecondComponent(new BorderLayoutPanel().addToTop(docLabel).addToCenter(docEditor.getComponent()));

        return splitter;
    }


    private void evaluate() {

        // setError("xxx");

        setResult("aaaaa");

        // JsonPathEvaluator.evaluate();

        // if (result instanceof IncorrectExpression) {
        //     setError(((IncorrectExpression) result).getMessage());
        // } else if (result instanceof IncorrectDocument) {
        //     setError(((IncorrectDocument) result).getMessage());
        // } else if (result instanceof ResultNotFound) {
        //     setError(((ResultNotFound) result).getMessage());
        // } else if (result instanceof ResultString) {
        //     setResult(((ResultString) result).getValue());
        // }
        //
        // if (result != null && !(result instanceof IncorrectExpression)) {
        //     addJSONPathToHistory(searchTextField.getText().trim());
        // }
    }

    private void setResult(String result) {
        WriteAction.run(() -> {
            Document document = resultEditor.getDocument();
            document.setText(result);
            PsiDocumentManager.getInstance(project).commitDocument(document);
            PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);

            assert psiFile != null;
            new ReformatCodeProcessor(psiFile, false).run();
        });

        // 切换展示视图
        if (!ArrayUtils.contains(resultWrapper.getComponents(), resultEditor.getComponent())) {
            resultWrapper.removeAll();
            resultWrapper.add(resultLabel, BorderLayout.NORTH);

            resultWrapper.add(resultEditor.getComponent(), BorderLayout.CENTER);
            resultWrapper.revalidate();
            resultWrapper.repaint();
        }

        resultEditor.getCaretModel().moveToOffset(0);
    }

    private void setError(String error) {
        errorOutputArea.setText(error);

        // 切换展示视图
        if (!ArrayUtils.contains(resultWrapper.getComponents(), errorOutputArea)) {
            resultWrapper.removeAll();
            resultWrapper.add(resultLabel, BorderLayout.NORTH);

            resultWrapper.add(errorOutputContainer, BorderLayout.CENTER);
            resultWrapper.revalidate();
            resultWrapper.repaint();
        }
    }


    private Editor createJsonEditor(String fileName, Boolean isViewer, EditorKind kind) {
        Editor editor = PlatformUtil.createEditor(project, fileName, FileTypeHolder.JSON5, isViewer, kind, "");
        editor.getSettings().setLineNumbersShown(false);
        return editor;
    }


    @Override
    public void dispose() {
        EditorFactory factory = EditorFactory.getInstance();
        factory.releaseEditor(resultEditor);
        factory.releaseEditor(docEditor);
    }


    public void setDocumentText(String text) {
        WriteAction.run(() -> {
            if (JsonUtil.isJson(text) || Json5Util.isJson5(text)) {
                docEditor.getDocument().setText(text);
            }
        });
    }

}
