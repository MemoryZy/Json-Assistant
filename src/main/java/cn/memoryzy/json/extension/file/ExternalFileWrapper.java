package cn.memoryzy.json.extension.file;

import cn.memoryzy.json.constant.FileTypeHolder;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VFileProperty;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * 外部文件包装类
 *
 * @author Memory
 * @since 2025/7/10
 */
public class ExternalFileWrapper extends VirtualFile {

    private final VirtualFile originalFile;

    public ExternalFileWrapper(VirtualFile originalFile) {
        this.originalFile = originalFile;
    }

    @Override
    public @NotNull String getName() {
        return originalFile.getName();
    }

    @Override
    public @NotNull FileType getFileType() {
        return FileTypeHolder.JSON5;
    }

    @Override
    public @NotNull VirtualFileSystem getFileSystem() {
        return originalFile.getFileSystem();
    }

    @Override
    public byte @Nullable [] getBOM() {
        return originalFile.getBOM();
    }

    @Override
    public boolean isRecursiveOrCircularSymlink() {
        return originalFile.isRecursiveOrCircularSymlink();
    }

    @Override
    public <T> T computeWithPreloadedContentHint(byte @NotNull [] preloadedContentHint, @NotNull Supplier<? extends T> computable) {
        return originalFile.computeWithPreloadedContentHint(preloadedContentHint, computable);
    }

    @Override
    public void setDetectedLineSeparator(@Nullable String separator) {
        originalFile.setDetectedLineSeparator(separator);
    }

    @Override
    public @Nullable String getDetectedLineSeparator() {
        return originalFile.getDetectedLineSeparator();
    }

    @Override
    public boolean isInLocalFileSystem() {
        return originalFile.isInLocalFileSystem();
    }

    @Override
    public boolean exists() {
        return originalFile.exists();
    }

    @Override
    public String toString() {
        return originalFile.toString();
    }

    @Override
    public void setBOM(byte @Nullable [] BOM) {
        originalFile.setBOM(BOM);
    }

    @Override
    public long getModificationCount() {
        return originalFile.getModificationCount();
    }

    @Override
    public @NotNull String getPresentableName() {
        return originalFile.getPresentableName();
    }

    @Override
    public void refresh(boolean asynchronous, boolean recursive) {
        originalFile.refresh(asynchronous, recursive);
    }

    @Override
    public long getModificationStamp() {
        return originalFile.getModificationStamp();
    }

    @Override
    public byte @NotNull [] contentsToByteArray(boolean cacheContent) throws IOException {
        return originalFile.contentsToByteArray(cacheContent);
    }

    @Override
    public void setBinaryContent(byte @NotNull [] content, long newModificationStamp, long newTimeStamp, Object requestor) throws IOException {
        originalFile.setBinaryContent(content, newModificationStamp, newTimeStamp, requestor);
    }

    @Override
    public void setBinaryContent(byte @NotNull [] content, long newModificationStamp, long newTimeStamp) throws IOException {
        originalFile.setBinaryContent(content, newModificationStamp, newTimeStamp);
    }

    @Override
    public boolean isCharsetSet() {
        return originalFile.isCharsetSet();
    }

    @Override
    public void setCharset(Charset charset, @Nullable Runnable whenChanged, boolean fireEventsWhenChanged) {
        originalFile.setCharset(charset, whenChanged, fireEventsWhenChanged);
    }

    @Override
    public void setCharset(Charset charset, @Nullable Runnable whenChanged) {
        originalFile.setCharset(charset, whenChanged);
    }

    @Override
    public void setCharset(Charset charset) {
        originalFile.setCharset(charset);
    }

    @Override
    public @NotNull Charset getCharset() {
        return originalFile.getCharset();
    }

    @Override
    public @NotNull VirtualFile copy(Object requestor, @NotNull VirtualFile newParent, @NotNull @NonNls String copyName) throws IOException {
        return originalFile.copy(requestor, newParent, copyName);
    }

    @Override
    public void move(Object requestor, @NotNull VirtualFile newParent) throws IOException {
        originalFile.move(requestor, newParent);
    }

    @Override
    public void delete(Object requestor) throws IOException {
        originalFile.delete(requestor);
    }

    @Override
    public @NotNull VirtualFile createChildData(Object requestor, @NotNull @NonNls String name) throws IOException {
        return originalFile.createChildData(requestor, name);
    }

    @Override
    public @NotNull VirtualFile createChildDirectory(Object requestor, @NotNull @NonNls String name) throws IOException {
        return originalFile.createChildDirectory(requestor, name);
    }

    @Override
    public @Nullable VirtualFile findFileByRelativePath(@NotNull @NonNls String relPath) {
        return originalFile.findFileByRelativePath(relPath);
    }

    @Override
    public @NotNull VirtualFile findOrCreateChildData(Object requestor, @NotNull @NonNls String name) throws IOException {
        return originalFile.findOrCreateChildData(requestor, name);
    }

    @Override
    public @Nullable VirtualFile findChild(@NotNull @NonNls String name) {
        return originalFile.findChild(name);
    }

    @Override
    public @Nullable VirtualFile getCanonicalFile() {
        return originalFile.getCanonicalFile();
    }

    @Override
    public @Nullable String getCanonicalPath() {
        return originalFile.getCanonicalPath();
    }

    @Override
    public boolean is(@NotNull VFileProperty property) {
        return originalFile.is(property);
    }

    @Override
    public void setWritable(boolean writable) throws IOException {
        originalFile.setWritable(writable);
    }

    @Override
    public void rename(Object requestor, @NotNull @NonNls String newName) throws IOException {
        originalFile.rename(requestor, newName);
    }

    @Override
    public @NotNull String getNameWithoutExtension() {
        return originalFile.getNameWithoutExtension();
    }

    @Override
    public @Nullable String getExtension() {
        return FileTypeHolder.JSON5.getDefaultExtension();
    }

    @Override
    public @NotNull String getUrl() {
        return originalFile.getUrl();
    }

    @Override
    public @NotNull Path toNioPath() {
        return originalFile.toNioPath();
    }

    @Override
    public @NotNull CharSequence getNameSequence() {
        return originalFile.getNameSequence();
    }

    @Override
    public @NonNls @NotNull String getPath() {
        return originalFile.getPath();
    }

    @Override
    public boolean isWritable() {
        return originalFile.isWritable();
    }

    @Override
    public boolean isDirectory() {
        return originalFile.isDirectory();
    }

    @Override
    public boolean isValid() {
        return originalFile.isValid();
    }

    @Override
    public VirtualFile getParent() {
        return originalFile.getParent();
    }

    @Override
    public VirtualFile[] getChildren() {
        return originalFile.getChildren();
    }

    @Override
    public @NotNull OutputStream getOutputStream(Object requestor, long newModificationStamp, long newTimeStamp) throws IOException {
        return originalFile.getOutputStream(requestor, newModificationStamp, newTimeStamp);
    }

    @Override
    public byte @NotNull [] contentsToByteArray() throws IOException {
        return originalFile.contentsToByteArray();
    }

    @Override
    public long getTimeStamp() {
        return originalFile.getTimeStamp();
    }

    @Override
    public long getLength() {
        return originalFile.getLength();
    }

    @Override
    public void refresh(boolean asynchronous, boolean recursive, @Nullable Runnable postRunnable) {
        originalFile.refresh(asynchronous, recursive, postRunnable);
    }

    @Override
    public @NotNull InputStream getInputStream() throws IOException {
        return originalFile.getInputStream();
    }


    @Override
    public <T> T getUserData(@NotNull Key<T> key) {
        return originalFile.getUserData(key);
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        originalFile.putUserData(key, value);
    }

    @Override
    public <T> T getCopyableUserData(@NotNull Key<T> key) {
        return originalFile.getCopyableUserData(key);
    }

    @Override
    public <T> void putCopyableUserData(@NotNull Key<T> key, T value) {
        originalFile.putCopyableUserData(key, value);
    }

    @Override
    public <T> boolean replace(@NotNull Key<T> key, @Nullable T oldValue, @Nullable T newValue) {
        return originalFile.replace(key, oldValue, newValue);
    }

    @Override
    public <T> @NotNull T putUserDataIfAbsent(@NotNull Key<T> key, @NotNull T value) {
        return originalFile.putUserDataIfAbsent(key, value);
    }

    @Override
    public boolean isUserDataEmpty() {
        return originalFile.isUserDataEmpty();
    }
}
