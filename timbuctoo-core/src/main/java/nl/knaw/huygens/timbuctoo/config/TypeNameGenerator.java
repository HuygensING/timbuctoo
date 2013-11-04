package nl.knaw.huygens.timbuctoo.config;

import nl.knaw.huygens.timbuctoo.annotations.EntityTypeName;
import nl.knaw.huygens.timbuctoo.model.Entity;

/**
 * A utility class that contains the logics to get the internal and external name of the class.
 * The "internal name" is used in the database, Solr index, etc. The "external name" is used in REST service.  
 */
public class TypeNameGenerator {
  public static String getInternalName(Class<?> type) {
    return type.getSimpleName().toLowerCase();
  }

  public static String getExternalName(Class<? extends Entity> type) {
    if (type.isAnnotationPresent(EntityTypeName.class)) {
      return type.getAnnotation(EntityTypeName.class).value();
    } else {
      return getInternalName(type) + "s";
    }
  }
}
