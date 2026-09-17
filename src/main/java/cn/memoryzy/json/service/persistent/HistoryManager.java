package cn.memoryzy.json.service.persistent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.constant.PathManager;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.state.JsonGroup;
import cn.memoryzy.json.service.persistent.state.JsonRecord;
import cn.memoryzy.json.service.persistent.state.RecordSerialization;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 每个项目都有一个实例
 *
 * @author Memory
 * @since 2025/12/12
 */
@Service(Service.Level.PROJECT)
public final class HistoryManager implements Disposable {

    private final Project project;

    private static final String HISTORIES_FILE_NAME = "histories.json";

    /**
     * 项目下的插件目录：
     * <br></br>
     * <p>- DIRECTORY_BASED: %PROJECT_DIR%/.idea/JsonAssistant/</p>
     * <p>- FILE_BASE: %LOCALAPPDATA%/MemoryZy/JsonAssistantPlugin/Projects/%PROJECT_DIR%/</p>
     */
    private final Path projectPluginDirectory;

    /**
     * 记录存放目录：
     * <br></br>
     * <p>- DIRECTORY_BASED: %PROJECT_DIR%/.idea/JsonAssistant/.histories/</p>
     * <p>- FILE_BASE: %LOCALAPPDATA%/MemoryZy/JsonAssistantPlugin/Projects/%PROJECT_DIR%/.histories/</p>
     */
    private final Path projectHistoriesDirectory;

    /**
     * 记录信息文件：
     * <br></br>
     * <p>- DIRECTORY_BASED: %PROJECT_DIR%/.idea/JsonAssistant/histories.json</p>
     * <p>- FILE_BASE: %LOCALAPPDATA%/MemoryZy/JsonAssistantPlugin/Projects/%PROJECT_DIR%/histories.json</p>
     */
    private final Path projectHistoriesFilePath;

    /**
     * 记录文件所在的目录
     */
    @JsonIgnore
    private volatile VirtualFile recordsDir;

    /**
     * 记录信息文件
     */
    private volatile VirtualFile historiesFile;


    /**
     * 最后存储时间
     */
    private Date lastSaved;

    /**
     * 历史记录信息
     */
    private List<JsonRecord> records;

    /**
     * 历史记录分组信息
     */
    private List<JsonGroup> groups;


    public HistoryManager(Project project) {
        this.project = project;
        this.projectPluginDirectory = PathManager.createAndGetProjectPluginDirectory(project);
        this.projectHistoriesDirectory = PathManager.createAndGetProjectHistoriesDirectory(projectPluginDirectory);
        this.projectHistoriesFilePath = projectPluginDirectory.resolve(HISTORIES_FILE_NAME);
        this.recordsDir = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(projectHistoriesDirectory);
        this.historiesFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(projectHistoriesFilePath);
        // 该实例初始化时执行，补充初始数据
        loadState();
    }


    // TODO 给记录增加自定义分组功能，并且可以定义某个记录是全局的还是只属于项目的

    public static HistoryManager getInstance(Project project) {
        return project.getService(HistoryManager.class);
    }


    // ---------------------------------- 记录/分组操作 ---------------------------------- //

    public synchronized JsonRecord addRecord(String content, JsonWrapper wrapper, String fileExtension, boolean checkRepeat) {
        if (checkRepeat && existsRecord(wrapper)) return null;

        JsonRecord record = new JsonRecord();
        record.setWrapper(wrapper);
        record.setFileExtension(fileExtension);
        record.setDisplayText(getShortText(wrapper));
        addRecord(record, content, true);
        return record;
    }

    public void addRecord(JsonRecord record, boolean writeState) {
        addRecord(record, null, writeState);
    }

    public void addRecord(JsonRecord record, String content, boolean writeState) {
        record.setDeleted(false);
        if (StrUtil.isBlank(record.getId())) record.setId(IdUtil.simpleUUID());
        if (null == record.getCreatedTime()) record.setCreatedTime(new Date());
        if (null == record.getUpdatedTime()) record.setUpdatedTime(new Date());
        record.setSourceFile(createSourceFile(record.getId(), record.getFileExtension(), content));
        records.add(record);

        // 与分组建立联系
        associateRecordWithGroup(record.getParentId(), record.getId());

        if (writeState) {
            // 写入文件
            saveState();
        }
    }

    public JsonRecord findRecord(String id) {
        return records.stream().filter(el -> el.getId().equals(id)).findFirst().orElse(null);
    }

    public List<JsonRecord> findRecords(Collection<String> ids) {
        if (CollUtil.isEmpty(ids)) return new ArrayList<>();
        return records.stream().filter(el -> ids.contains(el.getId())).collect(Collectors.toList());
    }

    /**
     * 查找相同结构的记录
     *
     * @param wrapper 结构
     * @return 记录
     */
    public JsonRecord findRecord(JsonWrapper wrapper) {
        return records.stream().filter(record -> null != record.getWrapper() && Objects.equals(wrapper, record.getWrapper())).findFirst().orElse(null);
    }

    /**
     * 判断是否已经存在
     *
     * @param wrapper 要添加的元素
     * @return 存在则返回true，不存在则返回false
     */
    public boolean existsRecord(JsonWrapper wrapper) {
        return null != findRecord(wrapper);
    }

    public boolean containsRecord(String recordId) {
        return null != findRecord(recordId);
    }


    public void delRecord(JsonRecord record) {
        // 删除记录
        records.removeIf(el -> Objects.equals(el.getId(), record.getId()));
        detachRecordFromGroup(record.getParentId(), record.getId());
        // 删除源文件
        delSourceFile(record);
        // 写入文件
        saveState();
    }


    public void delRecords(Collection<String> ids) {
        delRecords(ids, true);
    }


    public void delRecords(Collection<String> ids, boolean writeState) {
        if (CollUtil.isEmpty(ids)) return;
        Iterator<JsonRecord> iterator = records.iterator();
        while (iterator.hasNext()) {
            JsonRecord record = iterator.next();
            if (ids.contains(record.getId())) {
                iterator.remove();
                detachRecordFromGroup(record.getParentId(), record.getId());
                delSourceFile(record);
            }
        }

        if (writeState) {
            // 写入文件
            saveState();
        }
    }

    public List<JsonRecord> findTopLevelRecords() {
        return records.stream().filter(el -> null == el.getParentId()).collect(Collectors.toList());
    }

    public void addGroup(JsonGroup group) {
        group.setDeleted(false);
        if (StrUtil.isBlank(group.getId())) group.setId(IdUtil.simpleUUID());
        if (null == group.getCreatedTime()) group.setCreatedTime(new Date());
        if (null == group.getUpdatedTime()) group.setUpdatedTime(new Date());

        groups.add(group);
        // 写入文件
        saveState();
    }

    public JsonGroup findGroup(String id) {
        return groups.stream().filter(el -> el.getId().equals(id)).findFirst().orElse(null);
    }


    /**
     * 删除组（需递归删除）
     *
     * @param group 组
     */
    public void delGroup(JsonGroup group) {
        delGroups(ListUtil.toList(group.getId()));
    }


    /**
     * 删除分组及其所有子分组和记录（队列实现）
     */
    public void delGroups(Collection<String> ids) {
        delGroups(ids, true);
    }


    /**
     * 删除分组及其所有子分组和记录（队列实现）
     */
    public void delGroups(Collection<String> ids, boolean writeState) {
        if (CollUtil.isEmpty(ids)) return;

        Set<String> recordIds = new HashSet<>();
        Set<String> groupIds = new HashSet<>();
        Queue<String> queue = new LinkedList<>(ids);

        // 使用队列进行广度优先遍历，收集所有要删除的分组ID
        while (CollUtil.isNotEmpty(queue)) {
            String currentId = queue.poll();

            // 避免重复处理
            if (groupIds.contains(currentId)) {
                continue;
            }

            groupIds.add(currentId);

            // 查找当前分组的所有子分组
            for (JsonGroup group : groups) {
                if (Objects.equals(currentId, group.getParentId())) {
                    queue.offer(group.getId());
                }
            }

            // 添加当前分组的记录ID
            for (JsonGroup group : groups) {
                if (Objects.equals(currentId, group.getId())) {
                    recordIds.addAll(group.getRecordIds());
                    break;
                }
            }
        }

        // 执行删除操作
        if (CollUtil.isNotEmpty(groupIds)) groups.removeIf(group -> groupIds.contains(group.getId()));
        // 兼顾文件删除
        if (CollUtil.isNotEmpty(recordIds)) delRecords(recordIds, false);
        // 写入文件
        if (writeState) saveState();
    }

    public void delAllRecords() {
        delAllRecords(true);
    }

    public void delAllRecords(boolean writeState) {
        records.clear();
        // 删除所有文件
        FileUtil.clean(projectHistoriesDirectory.toFile());
        // 写入文件
        if (writeState) saveState();
    }

    public void delAllGroups() {
        groups.clear();
        delAllRecords(false);
        saveState();
    }


    /**
     * 根据 parentId 查找子分组
     *
     * @param parentId 父ID
     * @return 子分组
     */
    public List<JsonGroup> findChildGroups(String parentId) {
        return groups.stream().filter(el -> Objects.equals(parentId, el.getParentId())).collect(Collectors.toList());
    }

    public List<JsonGroup> findTopLevelGroups() {
        return groups.stream().filter(el -> null == el.getParentId()).collect(Collectors.toList());
    }

    public void associateRecordWithGroup(String groupId, String recordId) {
        associateRecordWithGroup(findGroup(groupId), recordId);
    }

    public void associateRecordWithGroup(JsonGroup group, String recordId) {
        if (null != group) group.getRecordIds().add(recordId);
    }

    public void detachRecordFromGroup(String groupId, String recordId) {
        detachRecordFromGroup(findGroup(groupId), recordId);
    }

    public void detachRecordFromGroup(JsonGroup group, String recordId) {
        if (null != group) group.getRecordIds().remove(recordId);
    }

    // ---------------------------------- 记录/分组操作 ---------------------------------- //


    private void loadState() {
        File historiesFile = projectHistoriesFilePath.toFile();
        if (historiesFile.exists() && historiesFile.length() > 0) {
            try {
                // 反序列化对象为 RecordSerialization，这是为了防止 HistoryManager 构造器触发 loadState 方法
                assignData(JsonUtil.MAPPER.readValue(historiesFile, RecordSerialization.class));

            } catch (IOException e) {
                // 把造成错误的 JSON 文件备份起来，留待之后检查
                String bakPath = HISTORIES_FILE_NAME + "." + DateUtil.format(new Date(), DatePattern.PURE_DATETIME_FORMATTER) + ".bak";
                Path dest = projectPluginDirectory.resolve(bakPath);
                FileUtil.move(historiesFile, dest.toFile(), true);
                assignData(null);
                throw new RuntimeException(e);
            }

        } else {
            assignData(null);
        }
    }

    public void saveState() {
        try {
            this.lastSaved = new Date();
            JsonUtil.MAPPER.writeValue(projectHistoriesFilePath.toFile(), new RecordSerialization(this));

            // 重新加载
            if (null == historiesFile) {
                // 如果没有过历史记录的项目，那么 histories.json 文件一开始就不存在，所以重新加载
                historiesFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(projectHistoriesFilePath);
            }

            if (null != historiesFile) {
                Document document = FileDocumentManager.getInstance().getCachedDocument(historiesFile);
                if (null != document) FileDocumentManager.getInstance().reloadFromDisk(document);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void assignData(RecordSerialization recordSerialization) {
        if (null == recordSerialization) {
            this.records = new ArrayList<>();
            this.groups = new ArrayList<>();

        } else {
            this.lastSaved = recordSerialization.getLastSaved();
            List<JsonRecord> newRecords = recordSerialization.getRecords();
            List<JsonGroup> newGroups = recordSerialization.getGroups();

            if (CollUtil.isNotEmpty(newRecords)) this.records = newRecords;
            else this.records = new ArrayList<>();

            if (CollUtil.isNotEmpty(newGroups)) this.groups = newGroups;
            else this.groups = new ArrayList<>();

            // 补充源文件对象
            replenishSourceFile();
        }
    }

    public void replenishSourceFile() {
        refreshDir();
        List<VirtualFile> children = ListUtil.toList(recordsDir.getChildren());

        for (JsonRecord record : records) {
            VirtualFile file = record.getSourceFile();
            if (PlatformUtil.isValidFile(file)) continue;
            // 组合文件名称
            String fileName = record.getId() + "." + record.getFileExtension();
            VirtualFile sourceFile = children.stream()
                    .filter(el -> null != el && !el.isDirectory() && StrUtil.equals(fileName, el.getName()))
                    .findFirst()
                    .orElse(null);
            // sourceFile 实例存在，但是源文件不存在
            if (null != sourceFile && !sourceFile.isValid()) {
                sourceFile = null;
            }

            // 添加标记，防止判定为外部文件不允许编辑
            PlatformUtil.markVirtualFileWritable(sourceFile, false);
            // 添加标记，标记为记录文件
            PlatformUtil.markVirtualFileFlag(sourceFile, project.getName());
            record.setSourceFile(sourceFile);

            // 在此获取源文件文本，用于构建 wrapper
            File scFile = projectHistoriesDirectory.resolve(fileName).toFile();
            if (scFile.exists()) {
                String content = FileUtil.readUtf8String(scFile);

                // 解析
                JsonWrapper wrapper = null;
                if (StrUtil.isNotBlank(content)) {
                    if (JsonUtil.isJson(content)) {
                        wrapper = JsonUtil.parse(content);

                    } else if (Json5Util.isJson5(content)) {
                        wrapper = Json5Util.parse(content);
                    }
                }

                if (null != wrapper && !wrapper.noItems()) record.setWrapper(wrapper);
            }
        }
    }

    public VirtualFile createSourceFile(String id, String extension, String content) {
        String fileName = id + "." + extension;
        Path filePath = projectHistoriesDirectory.resolve(fileName);
        File file = filePath.toFile();
        if (StrUtil.isNotBlank(content)) {
            FileUtil.writeUtf8String(content, file);
        } else {
            FileUtil.touch(file);
        }

        refreshDir();
        VirtualFile childFile = findChildFile(fileName);
        // 添加标记，防止判定为外部文件不允许编辑
        PlatformUtil.markVirtualFileWritable(childFile, false);
        // 添加标记，标记为记录文件
        PlatformUtil.markVirtualFileFlag(childFile, project.getName());
        return childFile;
    }

    public void delSourceFile(JsonRecord record) {
        // 删除源文件
        VirtualFile sourceFile = record.getSourceFile();
        if (null != sourceFile) {
            FileUtil.del(sourceFile.toNioPath());
            refreshDir();

        } else {
            // 拼接
            Path path = projectHistoriesDirectory.resolve(record.getId() + "." + record.getFileExtension());
            if (FileUtil.exist(path.toFile())) {
                FileUtil.del(path);
                refreshDir();
            }
        }
    }

    public VirtualFile findChildFile(String fileNameWithExtension) {
        for (VirtualFile child : recordsDir.getChildren()) {
            if (!child.isDirectory() && Objects.equals(child.getName(), fileNameWithExtension)) {
                return child;
            }
        }
        return null;
    }

    public void refreshDir() {
        refreshDir(false);
    }

    public void refreshDir(boolean asynchronous) {
        reloadDir();
        recordsDir.refresh(asynchronous, false);
    }

    private synchronized void reloadDir() {
        this.recordsDir = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(projectHistoriesDirectory);
    }

    public Date getLastSaved() {
        return lastSaved;
    }

    public void setLastSaved(Date lastSaved) {
        this.lastSaved = lastSaved;
    }

    public List<JsonRecord> getRecords() {
        return records;
    }

    public void setRecords(List<JsonRecord> records) {
        this.records = records;
    }

    public List<JsonGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<JsonGroup> groups) {
        this.groups = groups;
    }

    @Override
    public void dispose() {

    }

    public static String getShortText(JsonWrapper wrapper) {
        String jsonString = JsonUtil.compressJson(wrapper);
        return JsonAssistantUtil.truncateText(Objects.requireNonNull(jsonString), 80, "...");
        // return StringUtil.convertLineSeparators(truncatedText, ContentChooser.RETURN_SYMBOL);
    }

}
