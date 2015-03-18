package nl.knaw.huygens.timbuctoo.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to mark that a field should not be stored in the database.
 */
@Target(value = ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DBIgnore {
  // This annotation will not be used by the MongoStorage.
}
