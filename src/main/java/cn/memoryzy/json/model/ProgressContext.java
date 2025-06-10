package cn.memoryzy.json.model;

import com.intellij.openapi.progress.ProgressIndicator;

import java.util.concurrent.atomic.AtomicInteger;

public class ProgressContext {
    private final ProgressIndicator indicator;
    private final double startProgress;
    private final double endProgress;
    private final AtomicInteger processedNodes = new AtomicInteger(0);
    private final int estimatedTotalNodes;

    public ProgressContext(ProgressIndicator indicator,
                           double startProgress,
                           double endProgress,
                           int estimatedTotalNodes) {
        this.indicator = indicator;
        this.startProgress = startProgress;
        this.endProgress = endProgress;
        this.estimatedTotalNodes = estimatedTotalNodes;
    }

    public void incrementProcessedNodes() {
        int count = processedNodes.incrementAndGet();
        if (count % 3000 == 0) { // 每处理100个节点更新一次进度
            updateProgress();
        }
    }

    private void updateProgress() {
        double progressRange = endProgress - startProgress;
        double fraction = Math.min(1.0, (double) processedNodes.get() / estimatedTotalNodes);
        double currentProgress = startProgress + fraction * progressRange;
        indicator.setFraction(currentProgress);
    }

    public void finishRecursion() {
        indicator.setFraction(endProgress);
    }
}