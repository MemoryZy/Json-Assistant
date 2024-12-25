package cn.memoryzy.json.action.deserializer;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.HtmlConstant;
import cn.memoryzy.json.service.persistent.state.DeserializerState;
import cn.memoryzy.json.ui.component.*;
import cn.memoryzy.json.util.JavaUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.ui.CheckBoxList;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Memory
 * @since 2024/12/23
 */
public class OptionsGroup extends DefaultActionGroup implements DumbAware {

    private final Module module;
    private final DeserializerState deserializerState;

    public OptionsGroup(DeserializerState deserializerState, Module module) {
        super(JsonAssistantBundle.messageOnSystem("dialog.deserialize.options.text"), true);
        this.module = module;
        this.deserializerState = deserializerState;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setIcon(AllIcons.General.Settings);
        // 使用本身的 actionPerformed 执行
        presentation.setPerformGroup(true);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        boolean hasFastJsonLib = JavaUtil.hasFastJsonLib(module);
        boolean hasFastJson2Lib = JavaUtil.hasFastJson2Lib(module);
        boolean hasJacksonLib = JavaUtil.hasJacksonLib(module);

        DefaultListModel<JCheckBox> listModel = createListModel(hasFastJsonLib, hasFastJson2Lib);
        CheckBoxList<JBCheckBox> checkBoxList = new CheckBoxList<>(listModel) {
            @Override
            protected boolean isEnabled(int index) {
                return ((OptionsCheckBox) getModel().getElementAt(index)).isFeatureEnabled();
            }
        };

        ComponentPopupBuilder popupBuilder = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(checkBoxList, null)
                .setFocusable(true)
                .setShowShadow(true)
                .setShowBorder(true)
                .setLocateByContent(true)
                .setNormalWindowLevel(true)
                .addListener(new SaveOptionsListener(listModel));

        String adText = getAdText(hasFastJsonLib, hasFastJson2Lib, hasJacksonLib);
        if (StrUtil.isNotBlank(adText)) {
            popupBuilder.setAdText(HtmlConstant.wrapHtml(adText));
        }

        popupBuilder.createPopup().showInBestPositionFor(e.getDataContext());
    }

    private String getAdText(boolean hasFastJsonLib, boolean hasFastJson2Lib, boolean hasJacksonLib) {
        // 取反，方便处理逻辑
        boolean missingFastJsonLib = !hasFastJsonLib;
        boolean missingFastJson2Lib = !hasFastJson2Lib;
        boolean missingJacksonLib = !hasJacksonLib;

        // 若缺少 FastJson/FastJson2 或 Jackson 依赖其中一个，就给出提示
        if ((missingFastJsonLib && missingFastJson2Lib) || missingJacksonLib) {
            StringBuilder builder = new StringBuilder(JsonAssistantBundle.messageOnSystem("popup.deserialize.options.missingDependencies.text"));
            List<String> missingLibraries = new ArrayList<>();

            // 如果 FastJson/FastJson2依赖都没有，那就提示FastJson
            if (missingFastJsonLib && missingFastJson2Lib) {
                missingLibraries.add("<u><b>FastJson</b></u>");
            }

            if (missingJacksonLib) {
                missingLibraries.add("<u><b>Jackson</b></u>");
            }

            builder.append(StrUtil.join("、", missingLibraries));
            return builder.toString();
        }

        return null;
    }

    private DefaultListModel<JCheckBox> createListModel(boolean hasFastJsonLib, boolean hasFastJson2Lib) {
        JBCheckBox fastJsonCheckBox = new FastJsonOptionsCheckBox(module, deserializerState);
        JBCheckBox fastJson2CheckBox = new FastJson2OptionsCheckBox(module, deserializerState);
        JBCheckBox jacksonCheckBox = new JacksonOptionsCheckBox(module, deserializerState);
        JBCheckBox keepCamelCheckBox = new KeepCamelOptionsCheckBox(deserializerState);
        DefaultListModel<JCheckBox> listModel = new DefaultListModel<>();

        // -- 如果同时存在 fastJson、fastJson2 依赖，那么同时展示选择
        // -- 如果只存在 fastJson 依赖，不存在 fastJson2 依赖，那么只展示 fastJson 选项，反之亦然
        if (hasFastJsonLib && hasFastJson2Lib) {
            listModel.addElement(fastJsonCheckBox);
            listModel.addElement(fastJson2CheckBox);

            // 限制单选
            ButtonGroup group = new ButtonGroup();
            group.add(fastJsonCheckBox);
            group.add(fastJson2CheckBox);

        } else if (!hasFastJsonLib && !hasFastJson2Lib) {
            // 如果两种都不存在，那就添加一个默认的
            listModel.addElement(fastJsonCheckBox);

        } else if (hasFastJsonLib) {
            listModel.addElement(fastJsonCheckBox);

        } else {
            listModel.addElement(fastJson2CheckBox);
        }

        listModel.addElement(jacksonCheckBox);
        listModel.addElement(keepCamelCheckBox);

        return listModel;
    }


    private static class SaveOptionsListener implements JBPopupListener {

        private final DefaultListModel<JCheckBox> listModel;

        public SaveOptionsListener(DefaultListModel<JCheckBox> listModel) {
            this.listModel = listModel;
        }

        @Override
        public void onClosed(@NotNull LightweightWindowEvent event) {
            // 弹窗关闭后，保存选中/未选中的状态
            for (int i = 0; i < listModel.size(); i++) {
                JCheckBox checkBox = listModel.getElementAt(i);
                ((OptionsCheckBox) checkBox).performed();
            }
        }
    }
}
