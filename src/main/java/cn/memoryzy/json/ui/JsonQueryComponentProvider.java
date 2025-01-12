package cn.memoryzy.json.ui;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.action.query.ShowOriginalTextAction;
import cn.memoryzy.json.action.query.SwitchAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.service.persistent.JsonAssistantPersistentState;
import cn.memoryzy.json.service.persistent.state.QueryState;
import cn.memoryzy.json.ui.panel.SearchWrapper;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.tools.SimpleActionGroup;
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
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/12/17
 */
public class JsonQueryComponentProvider implements Disposable {

    public static final String SPLITTER_PROPORTION_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".SplitterProportionKey";
    public static final Key<Boolean> EDITOR_FLAG = Key.create(JsonAssistantPlugin.PLUGIN_ID_NAME + ".EditorFlag");

    private final Project project;
    private final SearchWrapper searchWrapper;
    private final JBPanelWithEmptyText resultWrapper;
    private final JBLabel resultLabel;
    private final Editor resultEditor;
    private final JBTextArea errorOutputArea;
    private final JBScrollPane errorOutputContainer;
    private final JBLabel docLabel;
    private final Editor docEditor;
    private final BorderLayoutPanel docPanel;

    private final QueryState queryState;


    public JsonQueryComponentProvider(Project project) {
        this.project = project;
        this.searchWrapper = new SearchWrapper(project, PlainTextFileType.INSTANCE, this::evaluate);

        this.resultWrapper = new JBPanelWithEmptyText(new BorderLayout());
        this.resultLabel = new JBLabel(JsonAssistantBundle.messageOnSystem("json.query.evaluate.result"));
        this.resultEditor = createJsonEditor("result.json5", true, EditorKind.PREVIEW);

        this.errorOutputArea = new JBTextArea();
        this.errorOutputContainer = new JBScrollPane(errorOutputArea);

        this.docLabel = new JBLabel(JsonAssistantBundle.messageOnSystem("json.query.evaluate.doc"));
        this.docEditor = createJsonEditor("original.json5", false, EditorKind.MAIN_EDITOR);
        this.docPanel = new BorderLayoutPanel().addToTop(docLabel).addToCenter(docEditor.getComponent());

        JsonAssistantPersistentState persistentState = JsonAssistantPersistentState.getInstance();
        this.queryState = persistentState.queryState;

        this.docPanel.setVisible(queryState.showOriginalText);
    }

    public JComponent createComponent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createFirstComponent(), BorderLayout.NORTH);
        panel.add(createSecondComponent(), BorderLayout.CENTER);

        SimpleToolWindowPanel simpleToolWindowPanel = new SimpleToolWindowPanel(true, false);
        simpleToolWindowPanel.setToolbar(createToolbar());
        simpleToolWindowPanel.setContent(panel);
        return simpleToolWindowPanel;
    }

    public JComponent createToolbar() {
        SimpleActionGroup actionGroup = new SimpleActionGroup();
        actionGroup.add(new SwitchAction(queryState));
        actionGroup.add(new ShowOriginalTextAction(queryState, this));

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, actionGroup, false);
        return toolbar.getComponent();
    }

    private JComponent createFirstComponent() {
        return searchWrapper;
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
        splitter.setSecondComponent(docPanel);

        return splitter;
    }


    private boolean evaluate(String path) {
        String doc = docEditor.getDocument().getText();
        if (Objects.isNull(path) || StrUtil.isBlank(doc)) {
            setError("xxx");
            return false;
        }



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

        return false;
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
        // 标记编辑器
        editor.putUserData(EDITOR_FLAG, true);
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

    public BorderLayoutPanel getDocPanel() {
        return docPanel;
    }
}
