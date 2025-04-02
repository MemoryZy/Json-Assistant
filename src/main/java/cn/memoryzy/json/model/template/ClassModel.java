package cn.memoryzy.json.model.template;

import java.util.*;

/**
 * @author Memory
 * @since 2025/4/2
 */
public class ClassModel implements TemplateModel {

    // @MapKey("PACKAGE_NAME")
    private String packageName;

    // @MapKey("NAME")
    private String className;

    @MapKey("CLASS_ANNOTATIONS")
    private final List<AnnotationModel> classAnnotations = new ArrayList<>();

    @MapKey("IMPORTS")
    private final Set<String> imports = new LinkedHashSet<>();

    @MapKey("FIELDS")
    private List<FieldModel> fields;


    // --------------------------

    public ClassModel addImports(String... imports) {
        Collections.addAll(this.imports, imports);
        return this;
    }

    public ClassModel addClassAnnotation(AnnotationModel annotation) {
        this.classAnnotations.add(annotation);
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

    public List<FieldModel> getFields() {
        return fields;
    }

    public ClassModel setFields(List<FieldModel> fields) {
        this.fields = fields;
        return this;
    }
}