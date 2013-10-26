package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.lang.reflect.Modifier;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;

import com.google.common.collect.Lists;

public class VariationUtils {

  public static final String AGREED = "a";
  public static final String VALUE = "v";
  public static final String BASE_MODEL_PACKAGE = "model";
  public static final String DEFAULT_VARIATION = "!defaultVRE";

  /**
   * Returns variation names for the specified entity type and its superclasses.
   */
  @SuppressWarnings("unchecked")
  public static List<String> getVariationNamesForType(Class<? extends Entity> type) {
    List<String> names = Lists.newArrayList();
    // TODO Use TypeRegistry, this loop is fragile
    while (type != null && !Modifier.isAbstract(type.getModifiers())) {
      names.add(typeToVariationName(type));
      type = (Class<? extends Entity>) type.getSuperclass();
    }
    return names;
  }

  public static String getPackageName(Class<? extends Entity> type) {
    String name = type.getPackage().getName();
    return name.substring(name.lastIndexOf('.') + 1);
  }

  public static String typeToVariationName(Class<? extends Entity> type) {
    String typeId = type.getSimpleName().toLowerCase();
    String variationId = getPackageName(type);
    return variationId.equals(BASE_MODEL_PACKAGE) ? typeId : variationId + "-" + typeId;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Entity> Class<? extends T> variationNameToType(TypeRegistry registry, String id) {
    return (Class<? extends T>) registry.getTypeForIName(normalize(id));
  }

  private static String normalize(String typeString) {
    return typeString.replaceFirst("[a-z]*-", "");
  }

}
