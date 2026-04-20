package cn.memoryzy.json.ui.panel;

import cn.memoryzy.json.constant.LanguageHolder;
import cn.memoryzy.json.enums.FileTypes;
import cn.memoryzy.json.util.JsonAssistantUtil;
import com.intellij.codeInsight.breadcrumbs.FileBreadcrumbsCollector;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorSettingsExternalizable;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider;
import com.intellij.ui.breadcrumbs.BreadcrumbsUtil;
import com.intellij.ui.components.breadcrumbs.Crumb;
import com.intellij.xml.breadcrumbs.BreadcrumbsForceShownSettings;
import com.intellij.xml.breadcrumbs.BreadcrumbsPresentationProvider;
import com.intellij.xml.breadcrumbs.BreadcrumbsXmlWrapper;
import com.intellij.xml.breadcrumbs.CrumbPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * 常驻面包屑面板
 *
 * @author Memory
 * @since 2026/4/20
 */
public class ResidentBreadcrumbsXmlWrapper extends BreadcrumbsXmlWrapper {

    private final VirtualFile file;
    private final EditorSettingsExternalizable settings;

    public ResidentBreadcrumbsXmlWrapper(@NotNull final Editor editor) {
        super(editor);
        file = FileDocumentManager.getInstance().getFile(myEditor.getDocument());
        settings = EditorSettingsExternalizable.getInstance();
    }

    @Nullable
    @Override
    protected Iterable<? extends Crumb> computeCrumbs(int offset) {
        FileBreadcrumbsCollector breadcrumbsCollector = findCollectorFor(myProject, file, this);
        if (breadcrumbsCollector == null) return null;

        Document document = myEditor.getDocument();
        Boolean forcedShown = BreadcrumbsForceShownSettings.getForcedShown(myEditor);

        // 如果开启面包屑并拥有 JSON 类型，则用原来的
        return settings.isBreadcrumbsShown() && settings.isBreadcrumbsShownFor("JSON")
                ? breadcrumbsCollector.computeCrumbs(file, document, offset, forcedShown)
                : computeCrumbs(file, document, offset, breadcrumbsCollector.getClass());
    }


    /**
     * 不受设置项开关的影响
     *
     * @param file     文件
     * @param document 文档
     * @param offset   偏移量
     * @param clz      面包屑收集器类
     * @return 面包屑集合
     */
    @SuppressWarnings("unchecked")
    private Iterable<Crumb> computeCrumbs(@NotNull VirtualFile file, @NotNull Document document, int offset, Class<? extends FileBreadcrumbsCollector> clz) {
        if (!FileTypes.JSON.getLanguageQualifiedName().equals(LanguageHolder.JSON.getClass().getName()))
            return new ArrayList<>();

        BreadcrumbsProvider defaultInfoProvider = BreadcrumbsUtil.getInfoProvider(LanguageHolder.JSON);
        // 反射调用
        Collection<Pair<PsiElement, BreadcrumbsProvider>> pairs =
                (Collection<Pair<PsiElement, BreadcrumbsProvider>>) JsonAssistantUtil.invokeStaticMethod(
                        clz, "getLineElements", document, offset, file, myProject, defaultInfoProvider, true);

        if (pairs == null) return Collections.emptyList();

        ArrayList<Crumb> result = new ArrayList<>(pairs.size());

        // 反射调用
        CrumbPresentation[] presentations = getCrumbPresentations(toPsiElementArray(pairs));

        int index = 0;
        for (Pair<PsiElement, BreadcrumbsProvider> pair : pairs) {
            CrumbPresentation presentation = null;
            if (presentations != null && 0 <= index && index < presentations.length) {
                presentation = presentations[index++];
            }

            // new PsiCrumb
            Class<?> crumbClz = JsonAssistantUtil.getClassByName("com.intellij.xml.breadcrumbs.PsiCrumb");
            Class<?>[] paramTypes = {PsiElement.class, BreadcrumbsProvider.class, CrumbPresentation.class};
            Crumb crumb = (Crumb) JsonAssistantUtil.newInstance(crumbClz, paramTypes, pair.first, pair.second, presentation);
            result.add(crumb);
        }

        return result;
    }


    private static PsiElement[] toPsiElementArray(Collection<? extends Pair<PsiElement, BreadcrumbsProvider>> pairs) {
        PsiElement[] elements = new PsiElement[pairs.size()];
        int index = 0;
        for (Pair<PsiElement, BreadcrumbsProvider> pair : pairs) {
            elements[index++] = pair.first;
        }
        return elements;
    }

    private static CrumbPresentation @Nullable [] getCrumbPresentations(final PsiElement[] elements) {
        for (BreadcrumbsPresentationProvider provider : BreadcrumbsPresentationProvider.EP_NAME.getExtensionList()) {
            final CrumbPresentation[] presentations = provider.getCrumbPresentations(elements);
            if (presentations != null) {
                return presentations;
            }
        }
        return null;
    }
}
