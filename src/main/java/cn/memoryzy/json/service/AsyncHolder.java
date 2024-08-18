package cn.memoryzy.json.service;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Memory
 * @since 2024/8/16
 */
public class AsyncHolder implements Disposable {

    public static AsyncHolder getInstance() {
        return ApplicationManager.getApplication().getService(AsyncHolder.class);
    }

    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
            2, 2, 1, TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(100));

    public void executeOnPooledThread(Runnable task){
        EXECUTOR.execute(task);
    }

    @Override
    public void dispose() {
        EXECUTOR.shutdownNow();
    }
}
