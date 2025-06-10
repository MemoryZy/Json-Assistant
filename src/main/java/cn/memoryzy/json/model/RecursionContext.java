package cn.memoryzy.json.model;

import com.sun.jdi.ObjectReference;

import java.util.HashSet;
import java.util.Set;

public class RecursionContext {
    private final int maxDepth;
    private int currentDepth = 0;
    private boolean recursionLimitReached = false;
    private final Set<Long> visitedObjects = new HashSet<>();
    
    public RecursionContext(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public boolean shouldRecurse(ObjectReference ref) {
        if (ref == null || currentDepth >= maxDepth) {
            if (currentDepth >= maxDepth) {
                recursionLimitReached = true;
            }
            return false;
        }

        long uid = ref.uniqueID();
        if (visitedObjects.contains(uid)) {
            return false;
        }

        visitedObjects.add(uid);
        return true;
    }

    public void enterObject() {
        currentDepth++;
    }

    public void exitObject() {
        currentDepth--;
    }

    public boolean isRecursionLimitReached() {
        return recursionLimitReached;
    }
    
    public int getCurrentDepth() {
        return currentDepth;
    }

    public void setCurrentDepth(int currentDepth) {
        this.currentDepth = currentDepth;
    }

    public int getMaxDepth() {
        return maxDepth;
    }
}