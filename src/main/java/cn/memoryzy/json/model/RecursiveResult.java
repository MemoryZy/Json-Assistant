package cn.memoryzy.json.model;

public class RecursiveResult {

    private final Object value;
    private final RecursionContext context;
    
    public RecursiveResult(Object value, RecursionContext context) {
        this.value = value;
        this.context = context;
    }
    
    public Object getValue() {
        return value;
    }
    
    public boolean isRecursionLimitReached() {
        return context != null && context.isRecursionLimitReached();
    }
    
    public int getCurrentDepth() {
        return context != null ? context.getCurrentDepth() : 0;
    }
    
    public int getMaxDepth() {
        return context != null ? context.getMaxDepth() : 0;
    }
    
    public boolean hasRecursionIssues() {
        return context != null && 
              (context.isRecursionLimitReached() || context.getCurrentDepth() > 0);
    }
}