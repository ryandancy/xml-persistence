package ca.keal.persistence;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated field should be persisted when the parent field is persisted.
 * 
 * The required {@link #value} (or simply the default) parameter specifies the name of the tag in which the annotated
 * field will be persisted. This is required and does <i>not</i> default to the name of the variable to encourage
 * forwards compatibility: it would be all too easy to change the variable's name and break compatibility with
 * previously persisted objects.
 * 
 * The type in which the annotated field is contained must be annotated {@code @Persistable}; otherwise, this annotation
 * will have no effect. This field's type must be a persistable type: either marked {@code @Persistable}, a primitive,
 * or a {@code String}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Persist {
  
  /**
   * The name of the tag in which the annotated field will be persisted in the XML file inside the parent class'
   * element. This must be a valid XML tag name and must be unique in the class hierarchy.
   */
  String value();
  
}
