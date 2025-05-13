package cn.memoryzy.json.model.template;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2025/4/2
 */
public class FieldModel implements TemplateModel {

    @MapKey("NAME")
    private String name;

    @MapKey("TYPE")
    private String type;

    @MapKey("COMMENT")
    private String comment;

    @MapKey("ANNOTATIONS")
    private List<AnnotationModel> annotations = new ArrayList<>();

    // --------------------------

    public FieldModel addAnnotation(AnnotationModel annotation) {
        this.annotations.add(annotation);
        return this;
    }

    // --------------------------

    public String getName() {
        return name;
    }

    public FieldModel setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public FieldModel setType(String type) {
        this.type = type;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public FieldModel setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public List<AnnotationModel> getAnnotations() {
        return annotations;
    }

    public FieldModel setAnnotations(List<AnnotationModel> annotations) {
        this.annotations = annotations;
        return this;
    }
}
