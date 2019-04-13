package ca.keal.persistence;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks types which may be persisted. When persisted, all fields in this class marked @{@link Persist} will be
 * persisted as well.
 * 
 * If {@link #toplevel} is set to {@code true} (default {@code false}), then a top-level element will be generated for
 * the instance of the annotated type in the top-level list. This must be used if a single object may be persisted
 * multiple times as otherwise multiple identical objects will be regenerated when a single shared object is persisted.
 * If {@link #toplevel} is {@code true}, then {@link #tag}, which is the name of the top-level tag, and
 * {@link #idField}, which is the name of a field holding an identifier, must be supplied. Otherwise, these parameters
 * are ignored.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Persistable {
  
  /**
   * If {@code true}, instances of this type will be serialized in the top-level element list. This must be done if
   * there is a chance that an object of this type will be serialized multiple times. Defaults to {@code false}.
   * 
   * If this is set to {@code true}, then {@link #idField} and {@link #tag} must also be supplied.
   */
  boolean toplevel() default false;
  
  /**
   * The name of a (non-static) field in the class that holds an ID. The field's {@code toString()} method will be used
   * to generate an identifier which will be used to identify the instance of the annotated type in the top-level list.
   * 
   * The field must exist in the class, must not be {@code null}, and must have a unique but deterministic value for
   * each instance of the annotated class. Its {@code toString()} method must return an ID appropriate for use in an
   * XML attribute.
   * 
   * This only needs to be defined if {@link #toplevel} is set to {@code true}; otherwise, it is ignored.
   */
  String idField() default "";
  
  /**
   * The name of the tag used in the top-level list to identify instances of this class. If supplied, this must be
   * unique among all @{@link Persistable} types with {@link #toplevel} set to {@code true}. This must also be a valid
   * XML tag name.
   * 
   * This only needs to be defined if {@link #toplevel} is set to {@code true}; otherwise, it is ignored.
   */
  String tag() default "";
  
}
