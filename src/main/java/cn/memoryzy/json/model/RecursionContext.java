package cn.memoryzy.json.model;

import com.sun.jdi.ObjectReference;

import java.util.HashSet;
import java.util.Set;

public class RecursionContext {
    private final int maxDepth;
    private int currentObjectDepth = 0;
    private boolean recursionLimitReached = false;
    private final Set<Long> visitedObjects = new HashSet<>();
    
    public RecursionContext(int maxDepth) {
        this.maxDepth = maxDepth;
    }
    
    public boolean shouldRecurse(ObjectReference ref) {
        if (ref == null) return false;
        
        // 深度检查
        if (currentObjectDepth >= maxDepth) {
            recursionLimitReached = true;
            return false;
        }
        
        // 循环引用检查
        long uid = ref.uniqueID();
        if (visitedObjects.contains(uid)) {
            return false;
        }
        
        visitedObjects.add(uid);
        return true;
    }
    
    public void enterObjectRecursion() {
        currentObjectDepth++;
    }
    
    public void exitObjectRecursion() {
        currentObjectDepth--;
    }
    
    public boolean isRecursionLimitReached() {
        return recursionLimitReached;
    }
    
    public int getCurrentDepth() {
        return currentObjectDepth;
    }
    
    public int getMaxDepth() {
        return maxDepth;
    }
}