package cn.memoryzy.json.model.debug;

import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import com.sun.jdi.Value;

/**
 * @author Memory
 * @since 2025/2/17
 */
public class ValueNodeData {

    private XValueNodeImpl node;

    private Value value;

    public ValueNodeData(XValueNodeImpl node, Value value) {
        this.node = node;
        this.value = value;
    }

    public boolean isNull() {
        return value == null;
    }


    public XValueNodeImpl getNode() {
        return node;
    }

    public void setNode(XValueNodeImpl node) {
        this.node = node;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

}
