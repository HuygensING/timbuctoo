package nl.knaw.huygens.timbuctoo.oaipmh;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface OAIDublinCoreField {
  DublinCoreMetadataField dublinCoreField();
}
