package cn.memoryzy.json.ui;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.ui.editor.SearchTextField;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.LightVirtualFile;
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
public class JsonQueryComponentProvider2 implements Disposable {

    public static final String JSON_PATH_HISTORY_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".JsonPathHistory";

    private final Project project;
    private final SearchTextField searchTextField;
    private final JBPanelWithEmptyText resultWrapper;
    private final JBLabel resultLabel;
    private final Editor resultEditor;
    private final JBTextArea errorOutputArea;
    private final JBScrollPane errorOutputContainer;
    private final JBLabel docLabel;
    private final Editor docEditor;

    public JsonQueryComponentProvider2(Project project) {
        this.project = project;
        this.searchTextField = new SearchTextField(JSON_PATH_HISTORY_KEY, 10, this::evaluate);

        this.resultWrapper = new JBPanelWithEmptyText(new BorderLayout());
        this.resultLabel = new JBLabel(JsonAssistantBundle.message("jsonpath.evaluate.result"));
        this.resultEditor = createJsonEditor("result.json5", true, EditorKind.PREVIEW);

        this.errorOutputArea = new JBTextArea();
        this.errorOutputContainer = new JBScrollPane(errorOutputArea);

        this.docLabel = new JBLabel(JsonAssistantBundle.message("jsonpath.evaluate.doc"));
        this.docEditor = createJsonEditor("doc.json5", false, EditorKind.MAIN_EDITOR);
    }

    public JComponent createComponent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createFirstComponent(), BorderLayout.NORTH);
        panel.add(createSecondComponent(), BorderLayout.CENTER);
        return panel;
    }


    private JComponent createFirstComponent() {
        return searchTextField;
    }

    private JComponent createSecondComponent() {
        // 一个Json原文编辑器（默认颜色），一个计算结果编辑器（跟随主界面）
        JBSplitter splitter = new JBSplitter(true, 0.5f);

        resultWrapper.getEmptyText().setText(JsonAssistantBundle.messageOnSystem("jsonpath.evaluate.no.result"));
        resultLabel.setBorder(JBUI.Borders.empty(3, 6));
        resultEditor.setBorder(JBUI.Borders.customLine(JBColor.border(), 1, 0, 0, 0));

        errorOutputArea.setEditable(false);
        errorOutputArea.setWrapStyleWord(true);
        errorOutputArea.setLineWrap(true);
        errorOutputArea.setBorder(JBUI.Borders.empty(10));
        errorOutputContainer.setBorder(JBUI.Borders.customLine(JBColor.border(), 1, 0, 0, 0));

        BorderLayoutPanel borderLayoutPanel = new BorderLayoutPanel()
                .addToTop(docLabel)
                .addToCenter(docEditor.getComponent());

        splitter.setFirstComponent(resultWrapper);
        splitter.setSecondComponent(borderLayoutPanel);

        return splitter;
    }


    private void evaluate() {
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
        // require strict JSON with quotes
        VirtualFile sourceVirtualFile = new LightVirtualFile(fileName, FileTypeHolder.JSON5, "");
        PsiFile sourceFile = PsiManager.getInstance(project).findFile(sourceVirtualFile);

        assert sourceFile != null;
        Document document = PsiDocumentManager.getInstance(project).getDocument(sourceFile);

        assert document != null;
        Editor editor = EditorFactory.getInstance().createEditor(document, project, sourceVirtualFile, isViewer, kind);

        editor.getSettings().setLineNumbersShown(false);
        return editor;
    }


    public static void main(String[] args) {
        // JBSplitter splitter = new JBSplitter(true, 0.5f);
        // splitter.setFirstComponent(borderLayoutPanel);
        // splitter.setSecondComponent(showTextField);
        //
        // // 保存分割比例
        // splitter.setSplitterProportionKey(showTextField);


    }


    @Override
    public void dispose() {
        EditorFactory.getInstance().releaseEditor(resultEditor);
    }
}
