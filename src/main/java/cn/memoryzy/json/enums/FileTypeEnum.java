package cn.memoryzy.json.enums;

/**
 * @author Memory
 * @since 2024/9/20
 */
public enum FileTypeEnum {

    JSON("com.intellij.json.JsonFileType", "INSTANCE", "com.intellij.json.JsonLanguage", "INSTANCE"),
    JSON5("com.intellij.json.json5.Json5FileType", "INSTANCE", "com.intellij.json.json5.Json5Language", "INSTANCE"),
    XML("com.intellij.ide.highlighter.XmlFileType", "INSTANCE", "com.intellij.lang.xml.XMLLanguage", "INSTANCE"),
    YAML("org.jetbrains.yaml.YAMLFileType", "YML", "org.jetbrains.yaml.YAMLLanguage", "INSTANCE"),
    TOML("org.toml.lang.psi.TomlFileType", "INSTANCE", "org.toml.lang.TomlLanguage", "INSTANCE");

    private final String fileTypeQualifiedName;
    private final String fileTypeInstanceFieldName;
    private final String languageQualifiedName;
    private final String languageInstanceFieldName;

    FileTypeEnum(String fileTypeQualifiedName, String fileTypeInstanceFieldName, String languageQualifiedName, String languageInstanceFieldName) {
        this.fileTypeQualifiedName = fileTypeQualifiedName;
        this.fileTypeInstanceFieldName = fileTypeInstanceFieldName;
        this.languageQualifiedName = languageQualifiedName;
        this.languageInstanceFieldName = languageInstanceFieldName;
    }

    public String getFileTypeQualifiedName() {
        return fileTypeQualifiedName;
    }

    public String getFileTypeInstanceFieldName() {
        return fileTypeInstanceFieldName;
    }

    public String getLanguageQualifiedName() {
        return languageQualifiedName;
    }

    public String getLanguageInstanceFieldName() {
        return languageInstanceFieldName;
    }
}
