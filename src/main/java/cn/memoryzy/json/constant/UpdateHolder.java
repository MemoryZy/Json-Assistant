package cn.memoryzy.json.constant;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Memory
 * @since 2024/8/6
 */
public class UpdateHolder {

    private static final AtomicBoolean isHasJsonStrAtomic = new AtomicBoolean(false);

    public static void supplementaryState(boolean isHasJsonStr) {
        isHasJsonStrAtomic.setRelease(isHasJsonStr);
    }

    public static boolean isHasJsonStr() {
        return isHasJsonStrAtomic.getAcquire();
    }


}
