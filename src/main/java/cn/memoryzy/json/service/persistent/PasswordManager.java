package cn.memoryzy.json.service.persistent;

import cn.memoryzy.json.JsonAssistantPlugin;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;

/**
 * @author Memory
 * @since 2025/6/10
 */
public class PasswordManager {

    /**
     * 唯一标识
     */
    private static final String key = "2a55a344efbc48d4bafcdea3b1ef6f29";

    /**
     * 插件在系统的表述
     */
    private static final String serviceName = CredentialAttributesKt.generateServiceName(JsonAssistantPlugin.PLUGIN_ID_NAME, key);

    public void savePassword(String password, String username) {
        if (username == null || password == null) return;
        PasswordSafe.getInstance().set(new CredentialAttributes(serviceName, username, this.getClass()), new Credentials(username, password));
    }

    public String getPassword(String username) {
        return PasswordSafe.getInstance().getPassword(new CredentialAttributes(serviceName, username, this.getClass()));
    }

}
