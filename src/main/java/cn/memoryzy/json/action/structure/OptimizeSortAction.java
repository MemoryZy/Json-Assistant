package cn.memoryzy.json.action.structure;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.action.JsonTextDiffAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.wrapper.ArrayWrapper;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAwareAction;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Memory
 * @since 2025/2/19
 */
public class OptimizeSortAction extends DumbAwareAction implements UpdateInBackground {

    public OptimizeSortAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.optimize.sort.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.optimize.sort.description"));
        presentation.setIcon(AllIcons.Actions.Refresh);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        List<DocumentContent> contentList = JsonTextDiffAction.getDiffContent(e.getDataContext());
        ImmutablePair<String, String> pair = JsonTextDiffAction.getContent(contentList);
        JsonWrapper leftObj = StructureComparisonAction.parseJson(pair.getLeft());
        JsonWrapper rightObj = StructureComparisonAction.parseJson(pair.getRight());

        Object rightObject = sortRightAccordingToLeft(leftObj, rightObj);
        DocumentContent documentContent = contentList.get(1);
        Document document = documentContent.getDocument();

        WriteAction.run(() -> {
            document.setText(Objects.requireNonNull(JsonUtil.formatJson(rightObject)));
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(isValid(e.getDataContext()));
    }

    private Object sortRightAccordingToLeft(Object leftObj, Object rightObj) {
        if (ObjectWrapper.isWrapper(leftObj) && ObjectWrapper.isWrapper(rightObj)) {
            // 处理JSON对象，按照左边的键顺序排序
            return sortMap((ObjectWrapper) leftObj, (ObjectWrapper) rightObj);

        } else if (ArrayWrapper.isWrapper(leftObj) && ArrayWrapper.isWrapper(rightObj)) {
            // 处理JSON数组，调整共同元素的顺序并递归处理子元素
            return sortList((ArrayWrapper) leftObj, (ArrayWrapper) rightObj);

        } else {
            // 非集合类型，直接返回右边的值
            return rightObj;
        }
    }

    private ObjectWrapper sortMap(ObjectWrapper leftObj, ObjectWrapper rightObj) {
        ObjectWrapper sortedMap = new ObjectWrapper();
        // 首先按照json1的键顺序添加存在的键
        for (String key : leftObj.keySet()) {
            if (rightObj.containsKey(key)) {
                Object leftValue = leftObj.get(key);
                Object rightValue = rightObj.get(key);
                sortedMap.put(key, sortRightAccordingToLeft(leftValue, rightValue));
            }
        }

        // 添加左边中存在但右边中没有的键，保持原顺序
        for (String key : rightObj.keySet()) {
            if (!leftObj.containsKey(key)) {
                sortedMap.put(key, rightObj.get(key));
            }
        }

        return sortedMap;
    }

    private List<Object> sortList(ArrayWrapper leftList, ArrayWrapper rightList) {
        List<Object> newList = new ArrayList<>();
        List<Object> remainingElements = new ArrayList<>(rightList);

        // 按照左边的顺序收集共同元素
        for (Object leftElement : leftList) {
            Iterator<Object> iterator = remainingElements.iterator();
            while (iterator.hasNext()) {
                Object rightElement = iterator.next();
                if (deepEquals(leftElement, rightElement)) {
                    newList.add(rightElement);
                    iterator.remove();
                    break;
                }
            }
        }

        // 添加剩余元素
        newList.addAll(remainingElements);

        // 递归处理每个元素，确保子结构也被处理
        List<Object> processedList = new ArrayList<>();
        for (int i = 0; i < newList.size(); i++) {
            Object rightElement = newList.get(i);
            Object leftElement = (i < leftList.size()) ? leftList.get(i) : null;
            if (leftElement != null) {
                processedList.add(sortRightAccordingToLeft(leftElement, rightElement));
            } else {
                processedList.add(rightElement);
            }
        }

        return processedList;
    }

    private boolean deepEquals(Object a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a instanceof Map && b instanceof Map) {
            Map<?, ?> mapA = (Map<?, ?>) a;
            Map<?, ?> mapB = (Map<?, ?>) b;
            if (mapA.size() != mapB.size()) return false;
            for (Map.Entry<?, ?> entry : mapA.entrySet()) {
                Object key = entry.getKey();
                if (!mapB.containsKey(key) || !deepEquals(entry.getValue(), mapB.get(key))) {
                    return false;
                }
            }
            return true;
        } else if (a instanceof List && b instanceof List) {
            List<?> listA = (List<?>) a;
            List<?> listB = (List<?>) b;
            if (listA.size() != listB.size()) return false;
            // 对于List，递归比较每个元素
            for (int i = 0; i < listA.size(); i++) {
                if (!deepEquals(listA.get(i), listB.get(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return a.equals(b);
        }
    }


    @SuppressWarnings("DuplicatedCode")
    private boolean isValid(DataContext dataContext) {
        List<DocumentContent> contentList = JsonTextDiffAction.getDiffContent(dataContext);
        if (CollUtil.isEmpty(contentList)) {
            return false;
        }

        ImmutablePair<String, String> immutablePair = JsonTextDiffAction.getContent(contentList);
        String leftText = immutablePair.getLeft();
        String rightText = immutablePair.getRight();

        if (StrUtil.isBlank(leftText) || StrUtil.isBlank(rightText)) {
            return false;
        }

        if (!JsonUtil.isJson(leftText) && !Json5Util.isJson5(leftText)) {
            return false;
        }

        return JsonUtil.isJson(rightText) || Json5Util.isJson5(rightText);
    }


}
