package cn.memoryzy.json.enums;

/**
 * @author Memory
 * @since 2024/9/20
 */
public enum FileTypes {

    JSON("json", "com.intellij.json.JsonFileType", "INSTANCE", "com.intellij.json.JsonLanguage", "INSTANCE"),
    JSON5("json5", "com.intellij.json.json5.Json5FileType", "INSTANCE", "com.intellij.json.json5.Json5Language", "INSTANCE"),
    JSONPATH("jsonpath", "com.intellij.jsonpath.JsonPathFileType", "INSTANCE", "com.intellij.jsonpath.JsonPathLanguage", "INSTANCE"),
    XML("xml", "com.intellij.ide.highlighter.XmlFileType", "INSTANCE", "com.intellij.lang.xml.XMLLanguage", "INSTANCE"),
    YAML("yaml", "org.jetbrains.yaml.YAMLFileType", "YML", "org.jetbrains.yaml.YAMLLanguage", "INSTANCE"),
    TOML("toml", "org.toml.lang.psi.TomlFileType", "INSTANCE", "org.toml.lang.TomlLanguage", "INSTANCE"),
    PROPERTIES("properties", "com.intellij.lang.properties.PropertiesFileType", "INSTANCE", "com.intellij.lang.properties.PropertiesLanguage", "INSTANCE"),
    TYPESCRIPT("ts", "com.intellij.lang.typescript.TypeScriptFileType", "INSTANCE", "com.intellij.lang.typescript.TypeScriptLanguage", "INSTANCE"),
    JAVA("java", "com.intellij.ide.highlighter.JavaFileType", "INSTANCE", "com.intellij.lang.java.JavaLanguage", "INSTANCE");

    private final String extension;
    private final String fileTypeQualifiedName;
    private final String fileTypeInstanceFieldName;
    private final String languageQualifiedName;
    private final String languageInstanceFieldName;

    FileTypes(String extension, String fileTypeQualifiedName, String fileTypeInstanceFieldName, String languageQualifiedName, String languageInstanceFieldName) {
        this.extension = extension;
        this.fileTypeQualifiedName = fileTypeQualifiedName;
        this.fileTypeInstanceFieldName = fileTypeInstanceFieldName;
        this.languageQualifiedName = languageQualifiedName;
        this.languageInstanceFieldName = languageInstanceFieldName;
    }

    public String getExtension() {
        return extension;
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
