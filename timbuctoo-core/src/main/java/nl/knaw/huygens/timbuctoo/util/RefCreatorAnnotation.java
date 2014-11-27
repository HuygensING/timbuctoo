package nl.knaw.huygens.timbuctoo.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for subtypes of {@link nl.knaw.huygens.model.Relation Relation}.
 */
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface RefCreatorAnnotation {
  Class<? extends RelationRefCreator> value();
}
