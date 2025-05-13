package cn.memoryzy.json.model.template;

import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Memory
 * @since 2025/4/2
 */
public class ClassModel implements TemplateModel {

    // @MapKey("PACKAGE_NAME")
    private String packageName;

    @MapKey("NAME")
    private String className;

    @MapKey("CLASS_ANNOTATIONS")
    private final List<AnnotationModel> classAnnotations = new ArrayList<>();

    @MapKey("IMPORTS")
    private Set<String> imports = new HashSet<>();

    @MapKey("FIELDS")
    private List<FieldModel> fields = new ArrayList<>();

    @MapKey("INNER_SNIPPETS")
    private List<String> innerSnippets = new ArrayList<>();

    @MapKey("IS_STATIC")
    private boolean isStatic = false;

    @MapKey("CLASS_MODIFIER")
    private String classModifier = "";

    // --------------------------


    public ClassModel() {
    }

    public ClassModel(boolean isStatic) {
        this.isStatic = isStatic;
        classModifier = isStatic ? "static " : "";
    }

    public ClassModel(String className, boolean isStatic) {
        this.className = className;
        this.isStatic = isStatic;
        classModifier = isStatic ? "static " : "";
    }

    public ClassModel addAnnotation(AnnotationModel anno) {
        this.classAnnotations.add(anno);
        return this;
    }

    public ClassModel addField(FieldModel fieldModel) {
        this.fields.add(fieldModel);
        return this;
    }


    public ClassModel addInnerSnippet(String inner) {
        if (StrUtil.isNotBlank(inner)) {
            this.innerSnippets.add(inner);
        }
        return this;
    }

    // --------------------------


    public String getPackageName() {
        return packageName;
    }

    public ClassModel setPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public ClassModel setClassName(String className) {
        this.className = className;
        return this;
    }

    public List<AnnotationModel> getClassAnnotations() {
        return classAnnotations;
    }

    public Set<String> getImports() {
        return imports;
    }

    public ClassModel setImports(Set<String> imports) {
        this.imports = imports;
        return this;
    }

    public List<FieldModel> getFields() {
        return fields;
    }

    public ClassModel setFields(List<FieldModel> fields) {
        this.fields = fields;
        return this;
    }

    public List<String> getInnerSnippets() {
        return innerSnippets;
    }

    public ClassModel setInnerSnippets(List<String> innerSnippets) {
        this.innerSnippets = innerSnippets;
        return this;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public ClassModel setStatic(boolean aStatic) {
        isStatic = aStatic;
        classModifier = aStatic ? "static " : "";
        return this;
    }

    public String getClassModifier() {
        return classModifier;
    }
}