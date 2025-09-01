// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package cn.memoryzy.json.ui.tree;

import cn.hutool.core.util.ReflectUtil;
import cn.memoryzy.json.util.JsonAssistantUtil;
import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.*;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.speedSearch.SpeedSearch;
import com.intellij.ui.speedSearch.SpeedSearchSupply;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ArrayUtil;
import com.intellij.util.EventDispatcher;
import com.intellij.util.Function;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.JBIterable;
import com.intellij.util.containers.JBIterator;
import com.intellij.util.containers.JBTreeTraverser;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.tree.TreeUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * 自定义可过滤元素的处理类（原先的{@link com.intellij.ui.FilteringTree} 无法使用，因为其在低版本IDE中是三个构造器，在高版本是两个构造器，无法同时兼容）
 *
 * @author Memory
 * @since 2025/8/20
 */
public abstract class FilterableTree<T extends DefaultMutableTreeNode, U> {

    @SuppressWarnings("unchecked")
    private static final Key<String> SEARCH_TEXT_KEY =
            (Key<String>) JsonAssistantUtil.readStaticFinalFieldValue(SpeedSearchBase.class, "SEARCH_TEXT_KEY");

    public static final SpeedSearchSupply DUMMY_SEARCH = new SpeedSearchSupply() {
        @Nullable
        @Override
        public Iterable<TextRange> matchingFragments(@NotNull String text) {
            return null;
        }

        @Override
        public void refreshSelection() {
        }

        @Override
        public boolean isPopupActive() {
            return false;
        }

        @Override
        public void addChangeListener(@NotNull PropertyChangeListener listener) {
        }

        @Override
        public void removeChangeListener(@NotNull PropertyChangeListener listener) {
        }

        @Override
        public void findAndSelectElement(@NotNull String searchQuery) {
        }
    };

    protected final T myRoot;
    protected final Tree myTree;
    protected final Project project;

    public FilterableTree(@Nullable Project project, @NotNull Tree tree, @NotNull T root) {
        myRoot = root;
        myTree = tree;
        this.project = project;
        // 重构树节点
        rebuildTree();
        // 配置树
        configureTree(myTree);
        myTree.setModel(new SearchTreeModel<>(myRoot, DUMMY_SEARCH, this::getText, this::createNode, this::getChildren, useIdentityHashing()));
    }

    @NotNull
    public SearchTextField installSearchField() {
        SearchTextField field = new SearchTextField(false) {
            @Override
            protected boolean preprocessEventForTextField(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN
                        || e.getKeyCode() == KeyEvent.VK_UP) {
                    myTree.dispatchEvent(e);
                    return true;
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE && getText().isEmpty()) {
                    IdeFocusManager.findInstance().requestFocus(myTree, true);
                    return true;
                }
                return false;
            }
        };

        getSearchModel().setSpeedSearch(createSpeedSearch(field));
        return field;
    }

    @NotNull
    public SearchTextField installBorderlessSearchField() {
        SearchTextField field = installSearchField();
        field.setOpaque(false);
        field.setBorder(JBUI.Borders.empty());

        JBTextField editor = field.getTextEditor();
        editor.setOpaque(false);
        editor.setBorder(JBUI.Borders.empty());
        return field;
    }

    public void installSimple() {
        SpeedSearchSupply supply = new TreeSpeedSearch(myTree, p -> StringUtil.notNullize(getText(p == null ? null : getUserObject((TreeNode) p.getLastPathComponent()))), true) {
            @Override
            protected void onSearchFieldUpdated(String pattern) {
                super.onSearchFieldUpdated(pattern);
                // constructor of popup
                if (StringUtil.isNotEmpty(pattern) && !isPopupActive()) {
                    SwingUtilities.invokeLater(() -> {
                        getSearchModel().refilter();
                        if (StringUtil.isNotEmpty(pattern))
                            TreeUtil.expandAll(myTree);
                    });
                } else {
                    getSearchModel().refilter();
                }
            }

            @Override
            public boolean isPopupActive() {
                JComponent mySearchPopup = (JComponent) ReflectUtil.getFieldValue(this, "mySearchPopup");
                JTextField searchField = getSearchField();

                // 窗口可见
                return (mySearchPopup != null && mySearchPopup.isVisible()
                        // 并且编辑器文本不为空
                        && null != searchField && StringUtil.isNotEmpty(searchField.getText()))
                        // 固定搜索弹窗并且文本不为空
                        || (isStickySearch() && StringUtil.isNotEmpty(ClientProperty.get(myComponent, Objects.requireNonNull(SEARCH_TEXT_KEY))));
            }
        };
        getSearchModel().setSpeedSearch(supply);
    }

    @NotNull
    protected SpeedSearchSupply createSpeedSearch(@NotNull SearchTextField searchTextField) {
        return new FilteringSpeedSearch(searchTextField);
    }

    protected class FilteringSpeedSearch extends MySpeedSearch<T> {

        protected FilteringSpeedSearch(@NotNull SearchTextField field) {
            super(myTree, field.getTextEditor());
        }

        @Override
        protected void onSearchFieldUpdated(String pattern) {
            TreePath[] paths = myTree.getSelectionModel().getSelectionPaths();
            getSearchModel().refilter();
            expandTreeOnSearchUpdateComplete(pattern);
            myTree.getSelectionModel().setSelectionPaths(paths);
            onSpeedSearchUpdateComplete(pattern);
        }

        @Override
        public void select(@NotNull T node) {
            TreeUtil.selectInTree(node, false, myTree);
        }

        @Override
        public boolean isMatching(@NotNull T node) {
            String text = getText(getUserObject(node));
            return text != null && matchingFragments(text) != null;
        }

        @Nullable
        @Override
        public T getSelection() {
            return ArrayUtil.getFirstElement(myTree.getSelectedNodes(getNodeClass(), null));
        }

        @NotNull
        @Override
        public Iterator<T> iterate(@Nullable T start, boolean fwd) {
            JBTreeTraverser<T> traverser = JBTreeTraverser.<T>from(n -> {
                int count = n.getChildCount();
                List<T> children = new ArrayList<>(count);
                for (int i = 0; i < count; ++i) {
                    T c = ObjectUtils.tryCast(n.getChildAt(fwd ? i : count - i - 1), getNodeClass());
                    if (c != null) children.add(c);
                }
                return children;
            }).expand(Conditions.alwaysTrue());
            if (start == null) {
                traverser = traverser.withRoot(getRoot());
            } else {
                List<T> roots = new ArrayList<>();
                for (TreeNode node = null, parent = start; parent != null; node = parent, parent = node.getParent()) {
                    int idx = node == null ? -1 : parent.getIndex(node);
                    for (int i = fwd ? idx + 1 : 0, c = fwd ? parent.getChildCount() : idx; i < c; ++i) {
                        T child = ObjectUtils.tryCast(parent.getChildAt(fwd ? i : idx - i - 1), getNodeClass());
                        if (child != null) roots.add(child);
                    }
                }
                traverser = traverser.withRoots(roots);
            }
            return traverser.preOrderDfsTraversal().iterator();
        }
    }

    protected abstract Class<? extends T> getNodeClass();

    @NotNull
    protected abstract T createNode(@NotNull U node);

    @NotNull
    protected abstract Iterable<U> getChildren(@NotNull U node);

    protected void configureTree(Tree tree) {
    }

    @NotNull
    public Tree getTree() {
        return myTree;
    }

    @NotNull
    public JComponent getComponent() {
        return myTree;
    }

    public Project getProject() {
        return project;
    }

    protected void rebuildTree() {
    }

    protected void expandTreeOnSearchUpdateComplete(@Nullable String pattern) {
        if (StringUtil.isNotEmpty(pattern)) TreeUtil.expandAll(myTree);
    }

    protected void onSpeedSearchUpdateComplete(@Nullable String pattern) {
    }

    protected boolean useIdentityHashing() {
        return true;
    }

    @Nullable
    protected abstract String getText(@Nullable U node);

    @NotNull
    @SuppressWarnings("unchecked")
    public SearchTreeModel<T, U> getSearchModel() {
        return (SearchTreeModel) myTree.getModel();
    }

    @NotNull
    public T getRoot() {
        return myRoot;
    }

    public void update() {
        rebuildTree();
        myTree.revalidate();
        myTree.repaint();
    }

    /**
     * 用于已经通过外部修改了根节点的情况
     */
    public void updateStructure() {
        getSearchModel().updateStructure();
        myTree.revalidate();
        myTree.repaint();
    }

    public static class SearchTreeModel<N extends DefaultMutableTreeNode, U> extends DefaultTreeModel {
        public interface Listener<U> extends EventListener {
            void beforeNodeChanged(U x);

            void nodeChanged(U x);
        }

        /**
         * 将节点对象(U)转换为显示文本的函数
         */
        private final @NotNull Function<? super U, String> myNamer;

        /**
         * 创建树节点的工厂函数
         */
        private final @NotNull Function<? super U, ? extends N> myFactory;

        /**
         * 树模型的根节点数据对象
         */
        private final U myRootObject;

        /**
         * 获取子节点的函数
         */
        private final Function<? super U, ? extends Iterable<? extends U>> myStructure;

        /**
         * 是否使用身份哈希（IdentityHashMap）进行缓存
         */
        private final boolean myUseIdentityHashing;

        /**
         * 过滤逻辑的提供者
         */
        private SpeedSearchSupply mySpeedSearch;

        /**
         * 缓存节点对象(U)到树节点(N)的映射
         */
        private Map<U, N> myNodeCache;

        /**
         * 节点变化事件分发器
         */
        @SuppressWarnings("unchecked")
        private final EventDispatcher<Listener<U>> myNodeChanged = (EventDispatcher) EventDispatcher.create(Listener.class);

        public SearchTreeModel(@NotNull N root, @NotNull SpeedSearchSupply speedSearch,
                               @NotNull Function<? super U, String> namer, @NotNull Function<? super U, ? extends N> nodeFactory,
                               @NotNull Function<? super U, ? extends Iterable<? extends U>> structure, boolean useIdentityHashing) {
            super(root);
            myRootObject = Objects.requireNonNull(getUserObject(root));
            mySpeedSearch = speedSearch;
            myNamer = namer;
            myFactory = nodeFactory;
            myStructure = structure;
            myUseIdentityHashing = useIdentityHashing;
            myNodeCache = createNodeCache();
            addTreeModelListener(new TreeModelListener() {
                @Override
                @SuppressWarnings("unchecked")
                public void treeNodesChanged(TreeModelEvent e) {
                    U object = getUserObject((N) e.getTreePath().getLastPathComponent());
                    if (object != null) myNodeChanged.getMulticaster().nodeChanged(object);
                }

                @Override
                public void treeNodesInserted(TreeModelEvent e) {
                }

                @Override
                public void treeNodesRemoved(TreeModelEvent e) {
                }

                @Override
                public void treeStructureChanged(TreeModelEvent e) {
                }
            });
        }

        public void modifyNode(@NotNull U object, @NotNull Runnable r) {
            myNodeChanged.getMulticaster().beforeNodeChanged(object);
            try {
                r.run();
            } finally {
                myNodeChanged.getMulticaster().nodeChanged(object);
            }
        }

        public void setSpeedSearch(@NotNull SpeedSearchSupply supply) {
            mySpeedSearch = supply;
            updateStructure();
        }

        public SpeedSearchSupply getSpeedSearch() {
            return mySpeedSearch;
        }

        public void addNodeListener(@NotNull Listener<U> listener) {
            myNodeChanged.addListener(listener);
        }

        public void updateStructure() {
            Map<U, N> newNodes = createNodeCache();
            for (U node : JBTreeTraverser.from(myStructure).withRoot(getRootObject())) {
                N treeNode = myNodeCache.get(node);
                newNodes.put(node, treeNode == null ? createNode(node) : treeNode);
            }
            List<N> oldNodes = new ArrayList<>();
            for (Map.Entry<U, N> entry : myNodeCache.entrySet()) {
                if (!newNodes.containsKey(entry.getKey())) oldNodes.add(entry.getValue());
            }
            myNodeCache = newNodes;
            for (N node : oldNodes) {
                if (node.getParent() != null) removeNodeFromParent(node);
            }
            refilter();
        }

        @Override
        @SuppressWarnings("unchecked")
        public N getRoot() {
            return (N) root;
        }

        @NotNull
        public U getRootObject() {
            return myRootObject;
        }

        @NotNull
        public N getNode(@NotNull U object) {
            N node = getCachedNode(object);
            if (node == null) myNodeCache.put(object, node = createNode(object));
            return node;
        }

        @Nullable
        public N getCachedNode(@Nullable U object) {
            if (object == null) return null;
            if (object == myRootObject) return getRoot();
            return myNodeCache.get(object);
        }

        @NotNull
        protected N createNode(@NotNull U object) {
            assert !(object instanceof DefaultMutableTreeNode);
            return myFactory.fun(object);
        }

        /**
         * 重新过滤树节点
         */
        public void refilter() {
            // 如果 SpeedSearch 拥有过滤条件
            if (mySpeedSearch.isPopupActive()) {
                Set<U> acceptCache = myUseIdentityHashing ? new ReferenceOpenHashSet<>() : new HashSet<>();
                // 计算能过滤通过的节点
                computeAcceptCache(myRootObject, acceptCache);
                // 按照计算过后的来过滤
                filterChildren(myRootObject, acceptCache::contains);
            } else {
                // 如果 SpeedSearch 没有过滤条件，那么恢复所有的节点
                filterChildren(myRootObject, x -> true);
            }
        }

        @NotNull
        private Map<U, N> createNodeCache() {
            return myUseIdentityHashing ? new IdentityHashMap<>() : new HashMap<>();
        }

        /**
         * 计算应显示的节点
         *
         * <pre>
         * 作用流程:
         *   1.递归遍历所有子节点
         *   2.判断当前节点是否应显示：
         *    • 根节点总是显示
         *    • 有名称且匹配搜索条件
         *   3.如果节点被接受：
         *    • 添加所有没有名称的子节点（即使不匹配）
         *    • 添加当前节点到缓存
         *
         * 关键点：
         *    • 使用后序遍历（先处理子节点）
         *    • 保持节点层次结构完整
         *    • 特殊处理没有名称的子节点
         * </pre>
         *
         * @param object 节点对象
         * @param cache  应显示的节点集合
         */
        private boolean computeAcceptCache(@NotNull U object, @NotNull Set<? super U> cache) {
            boolean isAccepted = false;
            // 遍历所有子节点
            Iterable<? extends U> children = getChildren(object);
            for (U child : children) {
                // 递归计算子节点
                isAccepted |= computeAcceptCache(child, cache);
            }

            // 判断当前节点是否应该显示
            String name = myNamer.fun(object);
            isAccepted |= object == myRootObject || name != null && accept(name);

            // 如果当前节点被接受
            if (isAccepted) {
                // 添加没有名称的子节点
                for (U child : children) {
                    if (myNamer.fun(child) == null) cache.add(child);
                }
                // 添加当前节点
                cache.add(object);
            }
            return isAccepted;
        }

        @NotNull
        public Iterable<? extends U> getChildren(@Nullable U object) {
            return object == null ? JBIterable.empty() : myStructure.fun(object);
        }

        @NotNull
        public Function<? super U, ? extends Iterable<? extends U>> getStructure() {
            return myStructure;
        }

        @Nullable
        private static <N extends DefaultMutableTreeNode> N getChildSafe(@NotNull N node, int i) {
            return node.getChildCount() <= i ? null : getChild(node, i);
        }

        @SuppressWarnings("unchecked")
        private static <N extends DefaultMutableTreeNode> N getChild(@NotNull N node, int i) {
            return (N) node.getChildAt(i);
        }

        /**
         * 应用过滤到树结构
         *
         * @param object 节点对象
         * @param filter 过滤函数
         */
        private void filterChildren(@Nullable U object, @NotNull Condition<? super U> filter) {
            if (object == null) return;
            // 获取对应的树节点
            N node = getNode(object);
            // 过滤直接子节点
            filterDirectChildren(node, filter);
            // 递归处理子节点
            for (int i = 0, c = node.getChildCount(); i < c; ++i) {
                filterChildren(getUserObject(getChild(node, i)), filter);
            }
        }

        /**
         * 直接子节点过滤
         *
         * @param node   节点
         * @param filter 过滤函数
         */
        private void filterDirectChildren(@NotNull N node, @NotNull Condition<? super U> filter) {
            // 创建接受节点的集合
            Set<U> accepted = new LinkedHashSet<>();

            // 遍历所有子节点对象
            for (U child : getChildren(getUserObject(node))) {
                if (filter.value(child)) accepted.add(child);
            }

            // 移除不被接受的节点
            removeNotAccepted(node, accepted);
            // 添加缺失的节点
            mergeAcceptedNodes(node, accepted);
        }

        /**
         * 添加匹配节点
         *
         * @param node     节点对象
         * @param accepted 允许通过的节点集合
         */
        private void mergeAcceptedNodes(@NotNull N node, Set<? extends U> accepted) {
            int k = 0;
            N cur = getChildSafe(node, 0);
            IntList newIds = new IntArrayList();
            // 遍历accepted集合
            for (U child : accepted) {
                U curUsrObject = getUserObject(cur);
                // 检查是否已存在
                boolean isCur = cur != null && myUseIdentityHashing ? curUsrObject == child : (curUsrObject != null && curUsrObject.equals(child));
                if (isCur) {
                    cur = getChildSafe(node, k + 1);
                } else {
                    newIds.add(k);
                    // 插入新节点
                    node.insert(getNode(child), k);
                }
                ++k;
            }

            // 触发节点添加事件
            if (!newIds.isEmpty()) {
                nodesWereInserted(node, newIds.toIntArray());
            }

            // 移除多余节点
            if (node.getChildCount() > k) {
                IntList leftIds = new IntArrayList();
                List<N> leftNodes = new ArrayList<>();
                for (int i = node.getChildCount() - 1; i >= k; --i) {
                    leftNodes.add(getChild(node, i));
                    node.remove(i);
                    leftIds.add(i);
                }
                if (!leftIds.isEmpty()) {
                    int[] ints = leftIds.toIntArray();
                    for (int i = 0; i < ints.length; i++) {
                        int temp = ints[i];
                        ints[i] = ints[ints.length - i - 1];
                        ints[ints.length - i - 1] = temp;
                    }
                    Collections.reverse(leftNodes);
                    nodesWereRemoved(node, ints, leftNodes.toArray());
                }
            }
        }

        /**
         * 移除不匹配节点
         *
         * @param node     节点对象
         * @param accepted 节点集合
         */
        private void removeNotAccepted(@NotNull N node, Set<U> accepted) {
            IntList removedIds = new IntArrayList();
            List<N> removedNodes = new ArrayList<>();

            // 从后往前遍历（避免索引变化）
            for (int i = node.getChildCount() - 1; i >= 0; --i) {
                N child = getChild(node, i);
                if (!accepted.contains(getUserObject(child))) {
                    removedIds.add(i);
                    removedNodes.add(child);
                    node.remove(i);
                }
            }

            // 触发节点移除事件
            if (!removedIds.isEmpty()) {
                Collections.reverse(removedNodes);
                int[] ints = removedIds.toIntArray();
                for (int i = 0; i < ints.length / 2; i++) {
                    int temp = ints[i];
                    ints[i] = ints[ints.length - i - 1];
                    ints[ints.length - i - 1] = temp;
                }
                nodesWereRemoved(node, ints, removedNodes.toArray());
            }
        }

        protected boolean accept(@Nullable String name) {
            if (name == null) return true;
            return mySpeedSearch.matchingFragments(name) != null;
        }

        @Override
        public boolean isLeaf(Object node) {
            return getRoot() != node && super.isLeaf(node);
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public final U getUserObject(@Nullable N node) {
            return node == null ? null : (U) node.getUserObject();
        }
    }

    private static class MySpeedSearch<Item> extends SpeedSearch {
        private boolean myUpdating = false;
        private final JTextComponent myField;

        protected void onUpdatePattern(@Nullable String text) {
        }

        MySpeedSearch(@NotNull JComponent comp, @NotNull JTextComponent field) {
            myField = field;
            myField.getDocument().addDocumentListener(new DocumentAdapter() {
                @Override
                protected void textChanged(@NotNull DocumentEvent e) {
                    if (!myUpdating) {
                        myUpdating = true;
                        try {
                            String text = myField.getText();
                            updatePattern(text);
                            onUpdatePattern(text);
                            update();
                        } finally {
                            myUpdating = false;
                        }
                    }
                }
            });
            setEnabled(true);
            comp.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (selectTargetElement(e.getKeyCode())) {
                        e.consume();
                    }
                }
            });
            comp.addKeyListener(this);
            installSupplyTo(comp);
        }

        @Override
        public void update() {
            String filter = getFilter();
            if (!myUpdating) {
                myUpdating = true;
                try {
                    myField.setText(filter);
                } finally {
                    myUpdating = false;
                }
            }
            onSearchFieldUpdated(filter);
            updateSelection();
        }

        @Override
        public void noHits() {
            myField.setBackground(LightColors.RED);
        }

        public void updateSelection() {
            Item selection = getSelection();
            if (selection != null && isMatching(selection)) return;
            JBIterator<Item> it = JBIterator.from(iterate(selection, true, true))
                    .filter(item -> item != selection && isMatching(item));
            if (!it.advance()) return;
            select(it.current());
        }

        protected void onSearchFieldUpdated(String pattern) {
        }

        public void select(@NotNull Item item) {
        }

        @Nullable
        public Item getSelection() {
            return null;
        }

        public boolean isMatching(@NotNull Item item) {
            return false;
        }

        @NotNull
        public Iterator<Item> iterate(@Nullable Item start, boolean fwd) {
            return new JBIterator<>() {
                @Override
                protected Item nextImpl() {
                    return stop();
                }
            };
        }

        @NotNull
        public Iterator<Item> iterate(@Nullable Item start, boolean fwd, boolean wrap) {
            if (!wrap || start == null) return iterate(start, fwd);
            return new JBIterator<>() {
                boolean wrapped = false;
                Iterator<Item> it = iterate(start, fwd);

                @Override
                protected Item nextImpl() {
                    if (it.hasNext()) return it.next();
                    if (wrapped) return stop();
                    wrapped = true;
                    it = JBIterator.from(iterate(null, fwd)).takeWhile(item -> item != start);
                    return it.hasNext() ? it.next() : stop();
                }
            };
        }

        private boolean selectTargetElement(int keyCode) {
            if (!isPopupActive()) return false;
            Iterator<Item> it;
            if (keyCode == KeyEvent.VK_UP) {
                it = iterate(getSelection(), false, UISettings.getInstance().getCycleScrolling());
            } else if (keyCode == KeyEvent.VK_DOWN) {
                it = iterate(getSelection(), true, UISettings.getInstance().getCycleScrolling());
            } else if (keyCode == KeyEvent.VK_HOME) {
                it = iterate(null, true);
            } else if (keyCode == KeyEvent.VK_END) {
                it = iterate(null, false);
            } else {
                return false;
            }
            it = JBIterator.from(it).filter(this::isMatching);
            if (it.hasNext()) {
                select(it.next());
            }
            return true;
        }

    }

    @Nullable
    @SuppressWarnings("unchecked")
    public final U getUserObject(@Nullable TreeNode node) {
        return node == null || !getNodeClass().isAssignableFrom(node.getClass()) ? null : (U) ((T) node).getUserObject();
    }

    public U getUserObject(@Nullable TreePath path) {
        if (null == path) return null;
        TreeNode node = (TreeNode) path.getLastPathComponent();
        return getUserObject(node);
    }

    public U getRootUserObject() {
        return getUserObject(getRoot());
    }
}
