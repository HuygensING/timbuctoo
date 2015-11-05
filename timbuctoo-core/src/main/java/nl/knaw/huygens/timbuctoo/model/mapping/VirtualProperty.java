package nl.knaw.huygens.timbuctoo.model.mapping;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation to help to determine properties used for searching and indexing.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface VirtualProperty {
  String propertyName();
}
