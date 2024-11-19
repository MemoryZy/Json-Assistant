package cn.memoryzy.json.service.persistent.state;

/**
 * 编辑器外观
 *
 * @author Memory
 * @since 2024/11/18
 */
public class EditorBehaviorState {

    /**
     * 自动导入最新 JSON 记录至编辑器
     */
    public boolean importHistory = false;


    // ---------------------------------

    /**
     * 自动识别并转换其他格式数据（总开关）
     */
    public boolean recognizeOtherFormats = true;

    /**
     * 自动识别并转换 XML 格式为 JSON 数据
     */
    public boolean recognizeXmlFormat = true;

    /**
     * 自动识别并转换 YAML 格式为 JSON 数据
     */
    public boolean recognizeYamlFormat = true;

    /**
     * 自动识别并转换 TOML 格式为 JSON 数据
     */
    public boolean recognizeTomlFormat = true;

    /**
     * 自动识别并转换 URL Param 格式为 JSON 数据
     */
    public boolean recognizeUrlParamFormat = true;

}
