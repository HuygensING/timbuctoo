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

  @SuppressWarnings("unchecked")
  public static List<Class<? extends Entity>> getAllClasses(Class<? extends Entity> cls) {
    List<Class<? extends Entity>> rv = Lists.newArrayList();
    Class<? extends Entity> myCls = cls;
    while (myCls != null && !Modifier.isAbstract(myCls.getModifiers())) {
      rv.add(myCls);
      myCls = (Class<? extends Entity>) myCls.getSuperclass();
    }
    return rv;
  }

  // Conversion between (domain) model type tokens and variation names.
  // The conversion rules are used by VariationReducer and VariationInducer only.

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
