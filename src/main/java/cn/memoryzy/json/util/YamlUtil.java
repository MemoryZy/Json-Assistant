package cn.memoryzy.json.util;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * @author Memory
 * @since 2024/9/19
 */
public class YamlUtil {

    public static String yamlToJson(String yamlStr) {
        Yaml yaml = yaml();
        Object load = yaml.load(yamlStr);
        return JsonUtil.objectToJson(load);
    }

    public static String jsonToYaml(String jsonStr) {
        Yaml yaml = yaml();
        Object obj = JsonUtil.jsonToObject(jsonStr);
        return yaml.dump(obj);
    }

    private static Yaml yaml() {
        // 设置 YAML 输出选项
        DumperOptions dumperOptions = new DumperOptions();
        // 设置 YAML 的输出样式为块样式（更易读）
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(dumperOptions);
    }

}
