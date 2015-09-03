package nl.knaw.huygens.timbuctoo.annotations;

import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to give a property a different name in the database.
 * This class provides functionality to have a different name in the database than exposed via the web service.
 */
@Target(value = ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DBProperty {
  String value();
  FieldType type() default FieldType.REGULAR;
}
